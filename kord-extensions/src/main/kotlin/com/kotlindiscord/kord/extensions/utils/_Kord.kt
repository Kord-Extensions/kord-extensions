package com.kotlindiscord.kord.extensions.utils

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.entity.User
import dev.kord.core.event.Event
import dev.kord.core.firstOrNull
import dev.kord.core.live.LiveKordEntity
import dev.kord.core.supplier.EntitySupplyStrategy
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.withTimeoutOrNull

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
@Suppress("ExpressionBodySyntax")
public suspend inline fun <reified T : Event> Kord.waitFor(
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
