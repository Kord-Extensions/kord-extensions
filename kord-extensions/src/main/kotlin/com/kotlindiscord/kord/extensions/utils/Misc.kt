package com.kotlindiscord.kord.extensions.utils

import com.gitlab.kordlib.core.Kord
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.firstOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.filterIsInstance

/**
 * Return the first received event that match the condition.
 *
 * @param T Event to wait for.
 * @param timeout Time before returning null, if no match can be done. Set to null to disable it.
 * @param condition Function return true if the event object is valid and should be returned.
 */
@Suppress("ExpressionBodySyntax")
suspend inline fun <reified T : Event> Kord.waitFor(
    timeout: Long? = null,
    noinline condition: (suspend T.() -> Boolean) = { true }
): T? {
    return if (timeout == null) {
        events.filterIsInstance<T>().firstOrNull(condition)
    } else {
        withTimeoutOrNull(timeout) {
            events.filterIsInstance<T>().firstOrNull(condition)
        }
    }
}

/**
 * Return a [Pair] made of the start of the string up to the first character matching the predicate excluded,
 * and the rest of the string.
 *
 * @param predicate Predicate used to determine the split.
 */
fun String.splitOn(predicate: (Char) -> Boolean): Pair<String, String> {
    val i = this.indexOfFirst(predicate)
    if (i == -1) {
        return Pair(this, "")
    }
    return Pair(this.slice(IntRange(0, i - 1)), this.slice(IntRange(i, this.length - 1)))
}

/**
 * Run a block of code within a coroutine scope, defined by a given dispatcher.
 *
 * This is intended for use with code that normally isn't designed to be run within a coroutine, such as
 * database actions.
 *
 * @param dispatcher The dispatcher to use - defaults to [Dispatchers.IO].
 * @param body The block of code to be run.
 */
suspend fun <T> runSuspended(dispatcher: CoroutineDispatcher = Dispatchers.IO, body: suspend CoroutineScope.() -> T) =
    withContext(dispatcher, body)

