package com.kotlindiscord.kord.extensions.checks

import dev.kord.core.event.Event
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
public fun or(vararg checks: CheckFun): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.or")

    suspend fun inner(event: Event): Boolean {
        return if (checks.any { it.invoke(event) }) {
            logger.passed()
            true
        } else {
            logger.failed("None of the given checks passed")
            false
        }
    }

    return ::inner
}

/** Infix-function version of [or]. **/
public infix fun (CheckFun).or(other: CheckFun): CheckFun = or(this, other)

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
public fun and(vararg checks: CheckFun): CheckFun {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.and")

    suspend fun inner(event: Event): Boolean {
        return if (checks.all { it.invoke(event) }) {
            logger.passed()
            true
        } else {
            logger.failed("At least one of the given checks failed")
            false
        }
    }

    return ::inner
}

/** Infix-function version of [and]. **/
public infix fun (CheckFun).and(other: CheckFun): CheckFun = and(this, other)
