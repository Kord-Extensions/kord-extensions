@file:JvmMultifileClass
@file:JvmName("MiscKt")

package com.kotlindiscord.kord.extensions.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogger
import mu.KotlinLogging

/**
 * Neutral and default logger
 */
public fun classLogger(): KLogger 
     = KotlinLogging.logger() {}

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


