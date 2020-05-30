package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.event.Event
import mu.KotlinLogging

/**
 * Special check that passes if any of the given checks pass.
 *
 * You can think of this as an `or` operation - pass it a bunch of checks, and
 * this one will return `true` if any of them pass.
 *
 * @param checks Two or more checks to combine.
 * @return Whether any of the checks passed.
 */
fun or(vararg checks: suspend (Event) -> Boolean): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        return if (checks.any { it.invoke(event) }) {
            logger.debug { "Passing check" }
            true
        } else {
            logger.debug { "Failing check: None of the given checks passed" }
            false
        }
    }

    return ::inner
}

/**
 * Special check that passes if all of the given checks pass.
 *
 * You can think of this as an `and` operation - pass it a bunch of checks, and
 * this one will return `true` if they all pass.
 *
 * Don't use this unless you're already using combinators. The `check` functions
 * can simply be passed multiple checks.
 *
 * @param checks Two or more checks to combine.
 * @return Whether all of the checks passed.
 */
fun and(vararg checks: suspend (Event) -> Boolean): suspend (Event) -> Boolean {
    val logger = KotlinLogging.logger {}

    suspend fun inner(event: Event): Boolean {
        return if (checks.all { it.invoke(event) }) {
            logger.debug { "Passing check" }
            true
        } else {
            logger.debug { "Failing check: At least one of the given checks failed" }
            false
        }
    }

    return ::inner
}
