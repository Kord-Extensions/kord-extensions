/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import com.kotlindiscord.kord.extensions.ExtensibleBot
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.entity.User
import dev.kord.core.event.Event
import dev.kord.core.live.LiveKordEntity
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

/** Flow containing all [User] objects in the cache. **/
public val Kord.users: Flow<User>
	get() = with(EntitySupplyStrategy.cache).users

/**
 * Return the first received event that matches the condition.
 *
 * @param T Event to wait for.
 * @param timeout Time before returning null, if no match can be done. Set to null to disable it.
 * @param condition Function return true if the event object is valid and should be returned.
 */
public suspend inline fun <reified T : Event> Kord.waitFor(
	timeout: Long? = null,
	noinline condition: (suspend T.() -> Boolean) = { true },
): T? = if (timeout == null) {
	events.filterIsInstance<T>().firstOrNull(condition)
} else {
	withTimeoutOrNull(timeout) {
		events.filterIsInstance<T>().firstOrNull(condition)
	}
}

/**
 * Return the first received event that matches the condition.
 *
 * @param T Event to wait for.
 * @param timeout Time before returning null, if no match can be done. Set to null to disable it.
 * @param condition Function return true if the event object is valid and should be returned.
 */
public suspend inline fun <reified T : Event> Kord.waitFor(
	timeout: Duration? = null,
	noinline condition: (suspend T.() -> Boolean) = { true },
): T? = if (timeout == null) {
	events.filterIsInstance<T>().firstOrNull(condition)
} else {
	withTimeoutOrNull(timeout) {
		events.filterIsInstance<T>().firstOrNull(condition)
	}
}

/**
 * Return the first received event that matches the condition.
 *
 * @param T Event to wait for.
 * @param timeout Time before returning null, if no match can be done. Set to null to disable it.
 * @param condition Function return true if the event object is valid and should be returned.
 */
@KordPreview
@Suppress("ExpressionBodySyntax")
public suspend inline fun <reified T : Event> LiveKordEntity.waitFor(
	timeout: Long? = null,
	noinline condition: (suspend T.() -> Boolean) = { true },
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
 * Return the first received event that matches the condition.
 *
 * @param T Event to wait for.
 * @param timeout Time before returning null, if no match can be done. Set to null to disable it.
 * @param condition Function return true if the event object is valid and should be returned.
 */
public suspend inline fun <reified T : Event> ExtensibleBot.waitFor(
	timeout: Duration? = null,
	noinline condition: (suspend T.() -> Boolean) = { true },
): T? = if (timeout == null) {
	events.filterIsInstance<T>().firstOrNull(condition)
} else {
	withTimeoutOrNull(timeout) {
		events.filterIsInstance<T>().firstOrNull(condition)
	}
}
