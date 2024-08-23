/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.sentry

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.extensions.SentryDataTypeBuilder
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.captures.SentryCapture
import dev.kordex.core.utils.runSuspended
import io.github.oshai.kotlinlogging.KotlinLogging
import io.sentry.Sentry
import io.sentry.SentryOptions
import io.sentry.UserFeedback
import io.sentry.protocol.SentryId
import org.koin.core.component.inject

/**
 * A class that wraps the Sentry APIs in order to make them a bit easier to integrate.
 */
public open class SentryAdapter : KordExKoinComponent {
	/** Whether Sentry integration has been enabled. **/
	public val enabled: Boolean get() = this._enabled

	private val logger = KotlinLogging.logger { }

	private val botSettings: ExtensibleBotBuilder by inject()
	private val settings get() = botSettings.extensionsBuilder.sentryExtensionBuilder

	private var _enabled: Boolean = false
	private val eventIds: MutableSet<SentryId> = mutableSetOf()

	@Suppress("TooGenericExceptionCaught")
	public suspend fun setCaptureTypes(capture: SentryCapture) {
		if (capture.hasAllowedTypes()) {
			return
		}

		val types = settings.dataTypeBuilder.clone()

		settings.dataTypeTransformers.forEach {
			try {
				it(types, capture)
			} catch (e: Exception) {
				logger.warn(e) {
					"Exception thrown in Sentry data type transformer"
				}
			}
		}

		capture.setAllowedTypes(types)
	}

	@Suppress("TooGenericExceptionCaught")
	public suspend fun checkCapturePredicates(capture: SentryCapture): Boolean {
		for (predicate in settings.predicates) {
			try {
				if (!predicate(capture)) {
					return false
				}
			} catch (e: Exception) {
				logger.warn(e) {
					"Exception thrown in Sentry capture predicate"
				}
			}
		}

		return true
	}

	/**
	 * Set up Sentry and enable Sentry integration.
	 *
	 * This function takes a lambda that matches Sentry's, albeit using a receiver
	 * function instead for brevity. Please see the Sentry documentation for
	 * information on how to configure it.
	 */
	public fun init(callback: (SentryOptions) -> Unit) {
		Sentry.init(callback)

		this._enabled = true
	}

	/**
	 * Convenience wrapper around the Sentry user feedback API.
	 *
	 * **Note:** Doesn't use the [SentryCapture] system, and thus ignores the [SentryDataTypeBuilder].
	 * Disable the Sentry feedback extension if you don't want these to be submitted.
	 */
	public suspend fun sendFeedback(
		id: SentryId,

		comments: String? = null,
		name: String? = null,

		removeId: Boolean = true,
	) {
		if (!enabled) error("Sentry integration has not yet been configured.")

		val feedback = UserFeedback(id)

		if (comments != null) feedback.comments = comments
		if (name != null) feedback.name = name

		runSuspended {
			Sentry.captureUserFeedback(feedback)
		}

		if (removeId) {
			removeEventId(id)
		}
	}

	/** Register an event ID that a user may provide feedback for. **/
	public fun addEventId(id: SentryId?): Boolean {
		id ?: return false

		return eventIds.add(id)
	}

	/** Given an event ID, check whether it's awaiting feedback. **/
	public fun hasEventId(id: SentryId?): Boolean {
		id ?: return false

		return eventIds.contains(id)
	}

	/** Remove an event ID that feedback has already been provided for. **/
	public fun removeEventId(id: SentryId?): Boolean {
		id ?: return false

		return eventIds.remove(id)
	}
}
