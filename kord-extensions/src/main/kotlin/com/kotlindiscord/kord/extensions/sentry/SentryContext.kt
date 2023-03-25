/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import io.sentry.*
import io.sentry.protocol.SentryId
import mu.KLogger
import mu.KotlinLogging
import org.koin.core.component.inject

/**
 * Context object for keeping track of Sentry breadcrumbs and providing convenient APIs for submitting them, along with
 * transaction functions.
 *
 * Generally speaking, you'll probably want to use this instead of touching Sentry (or the adapter) directly.
 */
public class SentryContext : KordExKoinComponent {
    /** Quick access to the Sentry adapter, if required. **/
    public val adapter: SentryAdapter by inject()

    /**
     * List of Sentry breadcrumbs referred to as part of this context, You likely won't need to touch this directly,
     * but it's available for more advanced use-cases.
     */
    public val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

    /** Create a transaction with the given name and operation, and use it to measure the given callable. **/
    public inline fun transaction(name: String, operation: String, body: (ITransaction).() -> Unit) {
        val transaction = Sentry.startTransaction(name, operation)

        transaction(transaction, body)
    }

    /** Use the given transaction to measure the given callable. **/
    public inline fun transaction(transaction: ITransaction, body: (ITransaction).() -> Unit) {
        try {
            body(transaction)
        } catch (t: Throwable) {
            transaction.throwable = t
            transaction.status = SpanStatus.INTERNAL_ERROR
        } finally {
            transaction.finish()
        }
    }

    /** Register a breadcrumb of the given [type], using the [builder] to modify it and add context. **/
    public inline fun breadcrumb(type: BreadcrumbType = BreadcrumbType.Default, builder: Breadcrumb.() -> Unit) {
        val breadcrumb = Breadcrumb()
        breadcrumb.type = type.name

        builder(breadcrumb)

        if (!type.requiredKeys.all { breadcrumb.data.containsKey(it) }) {
            val logger: KLogger = KotlinLogging.logger("com.kotlindiscord.kord.extensions.sentry.SentryContext")

            logger.warn {
                "Ignoring provided breadcrumb type \"${type.name}\" - the following data keys are required: " +
                    type.requiredKeys.joinToString()
            }

            breadcrumb.type = BreadcrumbType.Default.name
        }

        breadcrumbs.add(breadcrumb)
    }

    /** Register a [breadcrumb] object that's already been created. **/
    public fun breadcrumb(breadcrumb: Breadcrumb): Boolean =
        breadcrumbs.add(breadcrumb)

    /** Capture a [SentryEvent], submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureEvent(
        event: SentryEvent,
        crossinline body: (Scope).() -> Unit
    ): SentryId {
        lateinit var id: SentryId

        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            id = Sentry.captureEvent(event)
        }

        adapter.addEventId(id)

        return id
    }

    /** Capture a [SentryEvent], submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureEvent(
        event: SentryEvent,
        hint: Hint,
        crossinline body: (Scope).() -> Unit
    ): SentryId {
        lateinit var id: SentryId

        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            id = Sentry.captureEvent(event, hint)
        }

        adapter.addEventId(id)

        return id
    }

    /** Capture a [Throwable] exception, submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureException(
        t: Throwable,
        crossinline body: (Scope).() -> Unit
    ): SentryId {
        lateinit var id: SentryId

        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            id = Sentry.captureException(t)
        }

        adapter.addEventId(id)

        return id
    }

    /** Capture a [Throwable] exception, submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureException(
        t: Throwable,
        hint: Hint,
        crossinline body: (Scope).() -> Unit
    ): SentryId {
        lateinit var id: SentryId

        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            id = Sentry.captureException(t, hint)
        }

        adapter.addEventId(id)

        return id
    }

    /** Capture a [UserFeedback] object, submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureFeedback(
        feedback: UserFeedback,
        crossinline body: (Scope).() -> Unit
    ) {
        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            Sentry.captureUserFeedback(feedback)
        }
    }

    /** Capture a [message] String, submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureMessage(
        message: String,
        crossinline body: (Scope).() -> Unit
    ): SentryId {
        lateinit var id: SentryId

        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            id = Sentry.captureMessage(message)
        }

        adapter.addEventId(id)

        return id
    }

    /** Capture a [message] String, submitting it to Sentry with the breadcrumbs in this context. **/
    public inline fun captureMessage(
        message: String,
        level: SentryLevel,
        crossinline body: (Scope).() -> Unit
    ): SentryId {
        lateinit var id: SentryId

        Sentry.withScope {
            body(it)

            breadcrumbs.forEach(it::addBreadcrumb)

            id = Sentry.captureMessage(message, level)
        }

        adapter.addEventId(id)

        return id
    }

    /** Make a copy of this Sentry context, bringing the breadcrumbs over into a new list. **/
    public fun copy(): SentryContext {
        val new = SentryContext()

        new.breadcrumbs.addAll(this.breadcrumbs)

        return new
    }
}
