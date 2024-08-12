/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlin.time.Duration

/**
 * Simple convenience function for mapping from `0` to the given [Int], exclusively.
 */
public inline fun <T> Int.map(body: (Int) -> T): List<T> =
	(0 until this).map<Int, T> { body(it) }

/**
 * Simple convenience function for mapping from `0` to the given [Long], exclusively.
 */
public inline fun <T> Long.map(body: (Long) -> T): List<T> =
	(0 until this).map<Long, T> { body(it) }

/**
 * Run a block of code within a coroutine scope, defined by a given dispatcher.
 *
 * This is intended for use with code that normally isn't designed to be run within a coroutine, such as
 * database actions.
 *
 * @param dispatcher The dispatcher to use - defaults to [Dispatchers.IO].
 * @param body The block of code to be run.
 */
public suspend fun <T> runSuspended(
	dispatcher: CoroutineDispatcher = Dispatchers.IO,
	body: suspend CoroutineScope.() -> T,
): T = withContext(dispatcher, body)

/** Retrieve the text from the footer of an embed builder, or `null` if no text was set. **/
public fun EmbedBuilder.Footer.textOrNull(): String? =
	try {
		text
	} catch (e: UninitializedPropertyAccessException) {
		null
	}

/**
 * Returns `true` if any element in the `Flow` matches the given predicate. Consumes the `Flow`.
 */
public suspend inline fun <T : Any> Flow<T>.any(crossinline predicate: suspend (T) -> Boolean): Boolean =
	firstOrNull { predicate(it) } != null

/**
 * Convert the given [DateTimePeriod] to a [Duration] based on the given timezone, relative to the current system time.
 */
public fun DateTimePeriod.toDuration(timezone: TimeZone): Duration {
	val now = Clock.System.now()

	return now.plus(this, timezone) - now
}
