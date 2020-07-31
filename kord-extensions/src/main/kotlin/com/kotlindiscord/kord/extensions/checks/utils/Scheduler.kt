package com.kotlindiscord.kord.extensions.checks.utils

import kotlinx.coroutines.*
import mu.KotlinLogging
import java.util.*

private val logger = KotlinLogging.logger { }

/**
 * Class in charge of providing scheduling functions.
 */
open class Scheduler {
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
    fun <T> schedule(delay: Long, data: T?, callback: suspend (T?) -> Unit): UUID {
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
    fun <T> schedule(id: UUID, delay: Long, data: T?, callback: suspend (T?) -> Unit) {
        logger.debug { "Scheduling task $id" }

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
    fun finishJob(id: UUID) {
        logger.debug { "Finishing task $id" }

        jobMap[id]?.cancel(finishTask)
    }

    /**
     * Cancel the provided task.
     *
     * @param id ID of the targeted task.
     */
    fun cancelJob(id: UUID) {
        logger.debug { "Canceling task $id" }

        jobMap[id]?.cancel()
    }

    /**
     * Immediately execute the callback of all the tasks belonging to this scheduler.
     */
    fun finishAll() {
        logger.debug { "Finishing all tasks." }

        for (id in jobMap.keys) {
            finishJob(id)
        }
    }

    /**
     * Cancel all the tasks belonging to this scheduler.
     */
    fun cancelAll() {
        logger.debug { "Canceling all tasks." }

        for (id in jobMap.keys) {
            cancelJob(id)
        }
    }

    /**
     * Get a scheduled job by ID.
     *
     * @param id ID of the targeted task.
     */
    fun getJob(id: UUID): Job? = jobMap[id]
}
