package com.kotlindiscord.kord.extensions.checks

import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
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
public fun or(vararg checks: Check<*>): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.or")

    val contexts = checks.map {
        val context = CheckContext(event, locale)

        it(context)
        context
    }

    if (contexts.any { it.passed }) {
        logger.passed()

        pass()
    } else {
        logger.failed("None of the given checks passed")

        val failedContext = contexts.firstOrNull { !it.passed }

        if (failedContext != null) {
            fail(failedContext.message)
        } else {
            fail()
        }
    }
}

/** Infix-function version of [or]. **/
public infix fun (Check<*>).or(other: Check<*>): Check<*> = or(this, other)

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
public fun and(vararg checks: Check<*>): Check<*> = {
    val logger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.checks.and")

    val contexts = checks.map {
        val context = CheckContext(event, locale)

        it(context)
        context
    }

    if (contexts.all { it.passed }) {
        logger.passed()

        pass()
    } else {
        logger.failed("At least one of the given checks failed")

        val failedContext = contexts.firstOrNull { !it.passed }

        if (failedContext != null) {
            fail(failedContext.message)
        } else {
            fail()
        }
    }
}

/** Infix-function version of [and]. **/
public infix fun (Check<*>).and(other: Check<*>): Check<*> = and(this, other)
