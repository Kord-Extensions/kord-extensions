@file:OptIn(ExperimentalTime::class)

package com.kotlindiscord.kord.extensions.utils.scheduling

import com.kotlindiscord.kord.extensions.utils.getKoin
import dev.kord.core.Kord
import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}

/**
 * Simple class representing a polling-based delayed task. Coroutine-based.
 *
 * @param duration How long to wait until the [callback] should be executed.
 * @param callback Callback to execute on task completion.
 * @param pollingSeconds How often to check whether enough time has passed - `1` by default.
 * @param coroutineScope Coroutine scope to launch in - Kord's by default.
 * @param parent Parent [Scheduler] object, if any.
 * @param name Optional task name, "Unnamed" by default.
 */
public class Task(
    public val duration: Duration,
    public val callback: suspend () -> Unit,
    public val pollingSeconds: Long = 1,
    public val coroutineScope: CoroutineScope = getKoin().get<Kord>(),
    public val parent: Scheduler? = null,

    name: String = "Unnamed",
) {
    private val logger = KotlinLogging.logger("Task: $name")
    private var job: Job? = null
    private lateinit var started: TimeMark

    /** Whether this task is currently running - that is, waiting until it's time to execute the [callback]. **/
    public val running: Boolean get() = job != null

    /** Calculate whether it's time to start this task, returning `true` if so. **/
    public fun shouldStart(): Boolean = started.elapsedNow() >= duration

    /** Mark the start time and begin waiting until the execution time has been reached. **/
    public fun start() {
        started = TimeSource.Monotonic.markNow()

        job = coroutineScope.launch {
            while (!shouldStart()) {
                @Suppress("MagicNumber")  // We're just turning it into seconds from millis
                delay(pollingSeconds * 1000)
            }

            @Suppress("TooGenericExceptionCaught")
            try {
                callback()
            } catch (t: Throwable) {
                logger.error(t) { "Error running scheduled callback." }
            }

            removeFromParent()

            job = null
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

        removeFromParent()
    }

    /** Stop waiting and don't execute. **/
    public fun cancel() {
        job?.cancel()
        job = null

        removeFromParent()
    }

    /** Like [cancel], but blocks .. **/
    public suspend fun cancelAndJoin() {
        job?.cancelAndJoin()
        job = null

        removeFromParent()
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
    public suspend fun join(): Unit? = job?.join()

    private fun removeFromParent() = parent?.removeTask(this@Task)
}
