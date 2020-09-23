package com.kotlindiscord.kord.extensions.test

import com.kotlindiscord.kord.extensions.utils.Scheduler
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SchedulerTest {
    private val scheduler = Scheduler()

    @Test
    fun `Tasks run exactly once`() {
        var count = 0;

        val uuid = scheduler.schedule(0, "") {
            count += 1
        }

        val job = scheduler.getJob(uuid)

        if (job != null && !job.isCompleted) {
            runBlocking {
                job.join()
            }
        }

        assertEquals(count, 1) { "Job executed $count times instead of once" }
    }
}
