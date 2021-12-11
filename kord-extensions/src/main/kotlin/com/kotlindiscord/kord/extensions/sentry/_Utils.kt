/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.sentry

import io.sentry.*
import io.sentry.Sentry.startTransaction
import io.sentry.protocol.User

/**
 * Convenience function to create and apply a Sentry User object to a scope.
 *
 * @param tag User's Discord tag
 * @param id User's Discord ID
 */
public fun Scope.user(tag: String, id: String) {
    val userObj = User()

    userObj.username = tag
    userObj.id = id

    this.user = userObj
}

/**
 * Convenience function to create and apply a Sentry User object to a scope.
 *
 * @param userObj Kord user object to add to this scope.
 */
public fun Scope.user(userObj: dev.kord.core.entity.User): Unit =
    user(userObj.tag, userObj.id.toString())

/**
 * Convenience function to quickly set a Sentry tag in the current scope.
 *
 * @param key Tag key
 * @param value Tag value
 */
public fun Scope.tag(key: String, value: String): Unit =
    setTag(key, value)

/**
 * Convenience function to quickly create a Sentry breadcrumb and apply it to the current scope.
 *
 * You'll probably want to use keyword arguments for this function. Take a look at the Sentry docs for more
 * information on the parameters.
 */
public fun Scope.breadcrumb(
    category: String? = null,
    level: SentryLevel? = null,
    message: String? = null,
    type: String? = null,
    hint: String? = null,

    data: Map<String, Any> = mapOf()
) {
    val breadcrumbObj = Breadcrumb()

    if (category != null) breadcrumbObj.category = category
    if (level != null) breadcrumbObj.level = level
    if (message != null) breadcrumbObj.message = message
    if (type != null) breadcrumbObj.type = type

    data.toSortedMap().forEach { (key, value) -> breadcrumbObj.setData(key, value) }

    this.addBreadcrumb(breadcrumbObj, hint)
}

/** Convenience function for creating and testing a sub-transaction. **/
public inline fun <T> ITransaction.transaction(name: String, operation: String, body: (ITransaction).() -> T) {
    val transaction = startTransaction(name, operation)

    transaction(transaction, body)
}

/** Convenience function for testing a sub-transaction. **/
public inline fun <T> ITransaction.transaction(transaction: ITransaction, body: (ITransaction).() -> T) {
    try {
        body(transaction)
    } catch (t: Throwable) {
        transaction.throwable = t
        transaction.status = SpanStatus.INTERNAL_ERROR
    } finally {
        transaction.finish()
    }
}
