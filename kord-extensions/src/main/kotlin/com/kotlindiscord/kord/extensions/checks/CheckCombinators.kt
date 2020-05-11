package com.kotlindiscord.kord.extensions.checks

import com.gitlab.kordlib.core.event.Event

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
    suspend fun inner(event: Event): Boolean = checks.any { it.invoke(event) }

    return ::inner
}
