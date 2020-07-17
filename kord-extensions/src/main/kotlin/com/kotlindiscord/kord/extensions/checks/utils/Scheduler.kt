package com.kotlindiscord.kord.extensions.checks.utils

import kotlinx.coroutines.*
import mu.KotlinLogging
import kotlin.coroutines.EmptyCoroutineContext

private val logger = KotlinLogging.logger {  }

/**
 * Class in charge of providing scheduling functions.
 */
class Scheduler {
    private val jobMap: MutableMap<Int, Job> = mutableMapOf()

    private val scope = CoroutineScope(EmptyCoroutineContext)
    private val finishTask = CancellationException()

    /**
     * Schedule a new task.
     *
     * @param id The ID of the task.
     * @param delay How long in milliseconds to wait before executing the callback.
     * @param data Arbitrary data to be passed to the callback.
     * @param callback Function to be executed.
     *
     * @return Return true on success, false otherwise.
     */
    fun <T> schedule(id: Int, delay: Long, data: T?, callback: (T?) -> Unit): Boolean {
        logger.debug { "Scheduling task $id" }

        if (id in jobMap) {
            logger.warn { "Cannot schedule task, ID already in use." }
            return false
        }

        val job = scope.launch {
            delay(delay)
            callback(data)
        }

        job.invokeOnCompletion {
            if (it == finishTask) {
                callback(data)
            }
        }

        jobMap[id] = job
        return true
    }

    /**
     * Immediately execute the callback of the provided task.
     *
     * @param id ID of the targeted task.
     */
    fun finishJob(id: Int) {
        logger.debug { "Finishing task $id" }
        jobMap[id]?.cancel(finishTask)
    }

    /**
     * Cancel the provided task.
     *
     * @param id ID of the targeted task.
     */
    fun cancelJob(id: Int) {
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
}
