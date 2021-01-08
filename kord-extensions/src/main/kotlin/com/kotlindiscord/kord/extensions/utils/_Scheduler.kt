@file:JvmMultifileClass
@file:JvmName("SchedulerKt")

package com.kotlindiscord.kord.extensions.utils

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.*

/**
 * Logger of the class
 */
private val LOG = classLogger()

/**
 * Class in charge of providing scheduling functions.
 */
public open class Scheduler {
    
    private val jobMap: MutableMap<UUID, Job> = mutableMapOf()
    private val scope = GlobalScope
    private val finishTask = CancellationException()

    /**
     * Schedule a new task, with a randomly generated ID.
     *
     * @param delay How long in milliseconds to wait before executing the callback.
     * @param data Arbitrary data to be passed to the callback.
     * @param callback Suspending function to be executed.
     *
     * @return Return true on success, false otherwise.
     */
    public fun <T> schedule(delay: Long, data: T?, callback: suspend (T?) -> Unit): UUID {
        val uuid = UUID.randomUUID()
        schedule(uuid, delay, data, callback)
        return uuid
    }

    /**
     * Schedule a new task.
     *
     * @param id The ID of the task.
     * @param delay How long in milliseconds to wait before executing the callback.
     * @param data Arbitrary data to be passed to the callback.
     * @param callback Suspending function to be executed.
     *
     * @return Return true on success, false otherwise.
     */
    public fun <T> schedule(id: UUID, delay: Long, data: T?, callback: suspend (T?) -> Unit) {
        LOG.debug { "Scheduling task $id" }

        if (id in jobMap) {
            throw IllegalArgumentException("Duplicate ID: $id")
        }

        val job = scope.launch {
            delay(delay)
            callback(data)
        }

        job.invokeOnCompletion {
            if (it == finishTask) {
                scope.launch {
                    callback(data)
                }
            }

            jobMap.remove(id)
        }

        jobMap[id] = job
    }

    /**
     * Immediately execute the callback of the provided task.
     *
     * @param id ID of the targeted task.
     */
    public fun finishJob(id: UUID) {
        LOG.debug { "Finishing task $id" }

        jobMap[id]?.cancel(finishTask)
    }

    /**
     * Cancel the provided task.
     *
     * @param id ID of the targeted task.
     */
    public fun cancelJob(id: UUID) {
        LOG.debug { "Canceling task $id" }

        jobMap[id]?.cancel()
    }

    /**
     * Immediately execute the callback of all the tasks belonging to this scheduler.
     */
    public fun finishAll() {
        LOG.debug { "Finishing all tasks." }
        jobMap.keys.forEach(this::finishJob)
    }

    /**
     * Cancel all the tasks belonging to this scheduler.
     */
    public fun cancelAll() {
        LOG.debug { "Canceling all tasks." }
        jobMap.keys.forEach(this::cancelJob)
    }

    /**
     * Get a scheduled job by ID.
     *
     * @param id ID of the targeted task.
     */
    public fun getJob(id: UUID): Job? = jobMap[id]
}
