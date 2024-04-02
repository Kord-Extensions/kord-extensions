/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import com.kotlindiscord.kord.extensions.sentry.captures.SentryBreadcrumbCapture
import com.kotlindiscord.kord.extensions.sentry.captures.SentryExceptionCapture
import com.kotlindiscord.kord.extensions.sentry.captures.SentryScopeCapture
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.runSuspended
import io.sentry.*
import io.sentry.protocol.SentryId
import org.koin.core.component.inject
import java.time.Instant
import java.util.*

/**
 * Context object for keeping track of Sentry breadcrumbs and providing convenient APIs for submitting them, along with
 * transaction functions.
 *
 * Generally speaking, you'll probably want to use this instead of touching Sentry (or the adapter) directly.
 */
public class SentryContext : KordExKoinComponent {
	/** Quick access to the Sentry adapter, if required. **/
	public val adapter: SentryAdapter by inject()

	/** Extra context to be submitted to Sentry along with any captures. **/
	public val extraContext: MutableStringKeyedMap<Any> = mutableMapOf()

	private val breadcrumbs: MutableList<Breadcrumb> = mutableListOf()

	/** Add an extra context value, to be submitted to Sentry along with any capture. **/
	public fun context(key: String, value: Any) {
		extraContext[key] = value
	}

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

	/** Register a breadcrumb of the given [type], using the [body] to modify it and add context. **/
	public suspend fun breadcrumb(
		type: BreadcrumbType = BreadcrumbType.Default,
		body: suspend SentryBreadcrumbCapture.() -> Unit,
	) {
		val breadcrumb = Breadcrumb(Date.from(Instant.now()))
		val capture = SentryBreadcrumbCapture(type)

		adapter.setCaptureTypes(capture)

		body(capture)

		capture.apply(breadcrumb)

		if (adapter.checkCapturePredicates(capture)) {
			breadcrumbs.add(breadcrumb)
		}
	}

	/** Capture a [SentryEvent], submitting it to Sentry with the breadcrumbs in this context. **/
	public suspend fun captureEvent(
		event: SentryEvent,
		body: suspend SentryScopeCapture.() -> Unit = {},
	): SentryId? {
		val capture = SentryScopeCapture()

		adapter.setCaptureTypes(capture)

		body(capture)

		if (adapter.checkCapturePredicates(capture)) {
			lateinit var id: SentryId

			runSuspended {
				Sentry.withScope {
					capture.apply(it)

					extraContext.forEach(it::setContexts)
					breadcrumbs.forEach(it::addBreadcrumb)

					id = Sentry.captureEvent(event)
				}
			}

			adapter.addEventId(id)

			return id
		}

		return null
	}

	/** Capture a [Throwable] exception, submitting it to Sentry with the breadcrumbs in this context. **/
	public suspend fun captureThrowable(
		t: Throwable,
		body: suspend (SentryExceptionCapture).() -> Unit = {},
	): SentryId? {
		val capture = SentryExceptionCapture(t)

		adapter.setCaptureTypes(capture)

		body(capture)

		if (adapter.checkCapturePredicates(capture)) {
			val id = capture.captureThrowable {
				capture.apply(it)

				extraContext.forEach(it::setContexts)
				breadcrumbs.forEach(it::addBreadcrumb)
			}

			adapter.addEventId(id)

			return id
		}

		return null
	}

	/** Capture a [UserFeedback] object, submitting it to Sentry with the breadcrumbs in this context. **/
	public suspend fun captureFeedback(
		feedback: UserFeedback,
		body: suspend SentryScopeCapture.() -> Unit = {},
	) {
		val capture = SentryScopeCapture()

		adapter.setCaptureTypes(capture)

		body(capture)

		if (adapter.checkCapturePredicates(capture)) {
			runSuspended {
				Sentry.withScope {
					capture.apply(it)

					extraContext.forEach(it::setContexts)
					breadcrumbs.forEach(it::addBreadcrumb)

					Sentry.captureUserFeedback(feedback)
				}
			}
		}
	}

	/** Capture a [message] String, submitting it to Sentry with the breadcrumbs in this context. **/
	public suspend fun captureMessage(
		message: String,
		body: suspend SentryScopeCapture.() -> Unit = {},
	): SentryId? {
		val capture = SentryScopeCapture()

		adapter.setCaptureTypes(capture)

		body(capture)

		if (adapter.checkCapturePredicates(capture)) {
			lateinit var id: SentryId

			runSuspended {
				Sentry.withScope {
					capture.apply(it)

					extraContext.forEach(it::setContexts)
					breadcrumbs.forEach(it::addBreadcrumb)

					id = Sentry.captureMessage(message)
				}
			}

			adapter.addEventId(id)

			return id
		}

		return null
	}

	/** Make a copy of this Sentry context, bringing the breadcrumbs over into a new list. **/
	public fun copy(): SentryContext {
		val new = SentryContext()

		new.breadcrumbs.addAll(this.breadcrumbs)

		return new
	}
}
