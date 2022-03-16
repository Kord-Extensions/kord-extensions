/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(ExperimentalTime::class)

package com.kotlindiscord.kord.extensions.utils.scheduling

import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.SentryContext
import com.kotlindiscord.kord.extensions.sentry.tag
import dev.kord.core.Kord
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Simple class representing a polling-based delayed task. Coroutine-based.
 *
 * @param duration How long to wait until the [callback] should be executed.
 * @param callback Callback to execute on task completion.
 * @param pollingSeconds How often to check whether enough time has passed - `1` by default.
 * @param coroutineScope Coroutine scope to launch in - Kord's by default.
 * @param parent Parent [Scheduler] object, if any.
 * @param name Optional task name, "Unnamed" by default.
 * @param repeat Whether the task should repeat after completion. `false` by default.
 */
public open class Task(
    public open val duration: Duration,
    public open val callback: suspend () -> Unit,
    public open val pollingSeconds: Long = 1,
    public open val coroutineScope: CoroutineScope = com.kotlindiscord.kord.extensions.utils.getKoin().get<Kord>(),
    public open val parent: Scheduler? = null,

    public val name: String = "Unnamed",
    public val repeat: Boolean = false
) : KoinComponent {
    protected val logger: KLogger = KotlinLogging.logger("Task: $name")
    protected var job: Job? = null
    protected lateinit var started: TimeMark
    protected val sentry: SentryAdapter by inject()

    /** Whether this task is currently running - that is, waiting until it's time to execute the [callback]. **/
    public val running: Boolean get() = job != null

    /** Calculate whether it's time to start this task, returning `true` if so. **/
    public fun shouldStart(): Boolean = started.elapsedNow() >= duration

    /** Mark the start time and begin waiting until the execution time has been reached. **/
    public fun start() {
        val sentryContext = SentryContext()

        started = TimeSource.Monotonic.markNow()

        sentryContext.breadcrumb(BreadcrumbType.Info) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

            message = "Starting task: waiting for configured delay to pass"

            data["delay"] = duration.toIsoString()
            data["name"] = name
            data["now"] = now.toString()
            data["pollingSeconds"] = pollingSeconds
        }

        job = coroutineScope.launch {
            while (!shouldStart()) {
                @Suppress("MagicNumber")  // We're just turning it into seconds from millis
                delay(pollingSeconds * 1000)
            }

            sentryContext.breadcrumb(BreadcrumbType.Info) {
                val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

                message = "Delay has passed, executing task"

                data["now"] = now.toString()
            }

            @Suppress("TooGenericExceptionCaught")
            try {
                callback()
            } catch (t: Throwable) {
                if (t is CancellationException && t.cause == null) {
                    logger.trace { "Task cancelled." }
                } else {
                    logger.error(t) { "Error running scheduled callback." }

                    if (sentry.enabled) {
                        sentryContext.captureException(t, "Error running scheduled callback") {
                            tag("task", name)
                        }
                    }
                }
            }

            if (!repeat) {
                removeFromParent()

                job = null
            } else {
                start()
            }
        }
    }

    /** Stop waiting and immediately execute the [callback]. **/
    public suspend fun callNow() {
        cancel()

        @Suppress("TooGenericExceptionCaught")
        try {
            callback()
        } catch (t: Throwable) {
            logger.error(t) { "Error running scheduled callback." }
        }
    }

    /** Stop waiting and don't execute. **/
    public fun cancel() {
        job?.cancel()
        job = null
    }

    /** Like [cancel], but blocks .. **/
    public suspend fun cancelAndJoin() {
        job?.cancelAndJoin()
        job = null
    }

    /** If the task is running, cancel it and restart it. **/
    public fun restart() {
        job?.cancel()
        job = null

        start()
    }

    /** Like [restart], but blocks until the cancellation has been applied. **/
    public suspend fun restartJoining() {
        job?.cancelAndJoin()
        job = null

        start()
    }

    /** Join the running [job], if any. **/
    public suspend fun join() {
        job?.join()
    }

    protected fun removeFromParent(): Boolean? = parent?.removeTask(this@Task)
}
