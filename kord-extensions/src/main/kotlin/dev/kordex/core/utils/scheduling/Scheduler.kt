/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:OptIn(ExperimentalTime::class)

package dev.kordex.core.utils.scheduling

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val logger = KotlinLogging.logger {}

/**
 * Simple task scheduler based on time-polling [Task] objects.
 *
 * Schedulers are [CoroutineScope]s and thus can be cancelled to cancel all nested jobs, if required..
 */
public open class Scheduler : CoroutineScope {
	internal val tasks: MutableList<Task> = mutableListOf()

	override val coroutineContext: CoroutineContext = Dispatchers.Default + SupervisorJob()

	/** Convenience function to schedule a [Task] using [seconds] instead of a [Duration]. **/
	public suspend fun schedule(
		seconds: Long,
		startNow: Boolean = true,
		name: String? = null,
		pollingSeconds: Long = 1,
		repeat: Boolean = false,
		callback: suspend () -> Unit,
	): Task = schedule(
		delay = seconds.seconds,
		startNow = startNow,
		name = name,
		pollingSeconds = pollingSeconds,
		repeat = repeat,
		callback = callback,
	)

	/**
	 * Schedule a [Task] using the given [delay] and [callback]. A name will be generated if not provided.
	 *
	 * @param delay [Duration] object representing the time to wait for.
	 * @param startNow Whether to start the task now - `false` if you want to start it yourself.
	 * @param name Optional task name, used in logging.
	 * @param pollingSeconds How often to check whether enough time has passed - `1` by default.
	 * @param repeat Whether to repeat the task indefinitely - `false` by default.
	 * @param callback Callback to run when the task has waited for long enough.
	 */
	public suspend fun schedule(
		delay: Duration,
		startNow: Boolean = true,
		name: String? = null,
		pollingSeconds: Long = 1,
		repeat: Boolean = false,
		callback: suspend () -> Unit,
	): Task {
		val taskName = name ?: UUID.randomUUID().toString()

		val task = Task(
			callback = callback,
			coroutineScope = this,
			pollingSeconds = pollingSeconds,
			duration = delay,
			name = taskName,
			repeat = repeat,
			parent = this
		)

		tasks.add(task)

		if (startNow) {
			task.start()
		}

		return task
	}

	/** Make all child tasks complete immediately. **/
	public suspend fun callAllNow(): Unit = tasks.forEach { it.callNow() }

	/** Shut down this scheduler, cancelling all tasks. **/
	public fun shutdown() {
		tasks.toList().forEach { it.cancel() }  // So we don't modify while we iterate

		try {
			this.cancel()
		} catch (e: IllegalStateException) {
			logger.debug(e) { "Scheduler cancelled with no jobs." }
		}

		tasks.clear()
	}

	internal fun removeTask(task: Task) =
		tasks.remove(task)
}
