/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.test

import com.kotlindiscord.kord.extensions.utils.scheduling.Scheduler
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchedulerTest {
    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `Schedulers can be cancelled`() = runBlocking {
        val scheduler = Scheduler()
        var count = 0

        scheduler.schedule(3) { count += 1 }
        scheduler.shutdown()

        delay(3000)

        assertEquals(count, 0) { "Task executed when it should have been cancelled" }
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `Tasks can be cancelled`() = runBlocking {
        val scheduler = Scheduler()
        var count = 0

        val task = scheduler.schedule(3) {
            count += 1
        }

        task.cancel()

        delay(3000)

        assertEquals(count, 0) { "Task executed when it should have been cancelled" }
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `Tasks remove themselves from the task list`() = runBlocking {
        val scheduler = Scheduler()
        var count = 0

        val tasks = mutableListOf<Task>()

        tasks += scheduler.schedule(10) { count += 1 }
        tasks += scheduler.schedule(10) { count += 1 }
        tasks += scheduler.schedule(10) { count += 1 }

        assertEquals(
            scheduler.tasks.size,
            tasks.size
        ) { "Scheduler should have ${tasks.size} tasks, but it has ${scheduler.tasks.size}" }

        tasks.forEach { it.callNow() }

        delay(1000)  // Some systems are a bit weird about job timing

        assertEquals(
            scheduler.tasks.size,
            0
        ) { "Scheduler should have 0 tasks, but it has ${scheduler.tasks.size}" }
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `Tasks run exactly once`() = runBlocking {
        val scheduler = Scheduler()
        var count = 0

        val task = scheduler.schedule(0) {
            count += 1
        }

        if (task.running) {
            task.join()
        }

        assertEquals(count, 1) { "Task executed $count times instead of once" }
    }

    @Test
    @Execution(ExecutionMode.CONCURRENT)
    fun `Tasks marked as repeatable run multiple times`() = runBlocking {
        val scheduler = Scheduler()
        var count = 0

        val task = scheduler.schedule(1.seconds, repeat = true) {
            count += 1
        }

        delay(10.seconds)

        task.cancel()

        assert(count > 1) { "Task did not run multiple times" }
    }
}
