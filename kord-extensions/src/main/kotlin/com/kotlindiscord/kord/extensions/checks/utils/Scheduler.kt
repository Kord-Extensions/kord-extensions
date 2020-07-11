package com.kotlindiscord.kord.extensions.checks.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Scheduler {
    private val jobMap: MutableMap<Int, Job> = mutableMapOf()

    fun <T> schedule(id: Int, delay: Long, data: T?, callback: (T?) -> Unit) {
        jobMap[id] = GlobalScope.launch {
            delay(delay)
            callback(data)
        }
    }
}
