/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils.scheduling

import dev.kord.core.Kord
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.sentry.SentryContext
import dev.kordex.core.utils.MutableStringKeyedMap
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
@OptIn(ExperimentalTime::class)
public open class Task(
	public open var duration: Duration,
	public open val callback: suspend () -> Unit,
	public open var pollingSeconds: Long = 1,
	public open val coroutineScope: CoroutineScope = dev.kordex.core.utils.getKoin().get<Kord>(),
	public open val parent: Scheduler? = null,

	public val name: String = "Unnamed",
	public val repeat: Boolean = false,
) : KordExKoinComponent {
	/** Cache map for storing data for this task, if needed. **/
	public val cache: MutableStringKeyedMap<Any> = mutableMapOf()

	protected val logger: KLogger = KotlinLogging.logger("Task: $name")
	protected var job: Job? = null
	protected lateinit var started: TimeMark
	protected val sentry: SentryAdapter by inject()

	/**
	 * Number of times this task has been executed.
	 *
	 * If a ULong is too small for this... wyd?
	 */
	public var executions: ULong = 0UL

	/** Whether this task is currently running - that is, waiting until it's time to execute the [callback]. **/
	public val running: Boolean get() = job != null

	/** Calculate whether it's time to start this task, returning `true` if so. **/
	public fun shouldStart(): Boolean = started.elapsedNow() >= duration

	/** Mark the start time and begin waiting until the execution time has been reached. **/
	public suspend fun start() {
		val sentryContext = SentryContext()

		started = TimeSource.Monotonic.markNow()

		if (executions == 0UL) {
			sentryContext.breadcrumb(BreadcrumbType.Info) {
				val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

				message = "Starting task: waiting for configured delay to pass"

				data["task.delay"] = duration.toIsoString()
				data["task.name"] = name
				data["task.pollingSeconds"] = pollingSeconds
				data["task.repeating"] = repeat

				data["time.now"] = now.toString()
			}
		}

		job = coroutineScope.launch {
			while (!shouldStart()) {
				@Suppress("MagicNumber")  // We're just turning it into seconds from millis
				delay(pollingSeconds * 1000)
			}

			if (executions == 0UL) {
				sentryContext.context("task", name)

				sentryContext.breadcrumb(BreadcrumbType.Info) {
					val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

					message = "Delay has passed, executing task (for the first time)"

					data["time.now"] = now.toString()
				}
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
						sentryContext.captureThrowable(t) {
							hints["executions"] = executions.toString()
						}
					}
				}
			} finally {
				if (executions == ULong.MAX_VALUE) {
					logger.debug { "Resetting execution counter as it has reached the maximum value." }

					executions = 0UL
				}

				executions += 1UL
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

		removeFromParent()
	}

	/** Like [cancel], but blocks .. **/
	public suspend fun cancelAndJoin() {
		job?.cancelAndJoin()
		job = null

		removeFromParent()
	}

	/** If the task is running, cancel it and restart it. **/
	public suspend fun restart() {
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

	protected fun removeFromParent(): Boolean? =
		parent?.removeTask(this@Task)
}
