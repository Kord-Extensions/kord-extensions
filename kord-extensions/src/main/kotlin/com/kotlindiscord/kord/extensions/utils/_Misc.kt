package com.kotlindiscord.kord.extensions.utils

import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
    body: suspend CoroutineScope.() -> T
): T = withContext(dispatcher, body)

/** Retrieve the text from the footer of an embed builder, or `null` if no text was set. **/
public fun EmbedBuilder.Footer.textOrNull(): String? =
    try {
        text
    } catch (e: UninitializedPropertyAccessException) {
        null
    }
