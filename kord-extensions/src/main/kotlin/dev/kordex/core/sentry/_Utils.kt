/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught")

package dev.kordex.core.sentry

import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildChannel
import io.sentry.IScope
import io.sentry.ITransaction
import io.sentry.Sentry.startTransaction
import io.sentry.SpanStatus
import io.sentry.protocol.User
import org.jetbrains.annotations.ApiStatus

/** Returns the Sentry-specific name for the given channel. **/
@get:ApiStatus.Internal
public val Channel.sentryName: String
	get() = when (this) {
		is DmChannel -> "[Private]"
		is GuildChannel -> name

		else -> "[Unknown]"
	}

/**
 * Convenience function to create and apply a Sentry User object to a scope.
 *
 * @param tag User's Discord tag
 * @param id User's Discord ID
 */
public fun IScope.user(tag: String, id: String) {
	val userObj = User()

	userObj.username = tag
	userObj.id = id

	this.user = userObj
}

/**
 * Convenience function to create and apply a Sentry User object to a scope.
 *
 * @param obj Kord user object to add to this scope.
 */
public fun IScope.user(obj: dev.kord.core.entity.User): Unit =
	user(obj.tag, obj.id.toString())

/** Convenience function for creating and testing a sub-transaction. **/
public inline fun <T> ITransaction.transaction(name: String, operation: String, body: ITransaction.() -> T) {
	val transaction = startTransaction(name, operation)

	transaction(transaction, body)
}

/** Convenience function for testing a sub-transaction. **/
public inline fun <T> ITransaction.transaction(transaction: ITransaction, body: ITransaction.() -> T) {
	try {
		body(transaction)
	} catch (t: Throwable) {
		transaction.throwable = t
		transaction.status = SpanStatus.INTERNAL_ERROR
	} finally {
		transaction.finish()
	}
}
