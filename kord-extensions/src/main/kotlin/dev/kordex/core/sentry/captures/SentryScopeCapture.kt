/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.sentry.captures

import dev.kordex.core.sentry.sentryName
import dev.kordex.core.sentry.user
import dev.kordex.core.utils.MutableStringKeyedMap
import io.sentry.IScope
import io.sentry.ITransaction
import io.sentry.Scope
import io.sentry.SentryLevel

/**
 * Base type representing a Sentry capture meant to populate a Sentry [Scope] object.
 *
 * This type exposes additional data types that are present within the [Scope] object.
 */
public open class SentryScopeCapture : SentryCapture() {
	/** An optional [SentryLevel] object to provide to the [Scope]. **/
	public open var level: SentryLevel? = null

	/** An optional [ITransaction] object to provide to the [Scope]. **/
	public open var transaction: ITransaction? = null

	/**
	 * Map of hint data to provide to the [Scope]. Supports the typed key syntax used by [processMap].
	 *
	 * For more information, see [the Sentry docs](https://develop.sentry.dev/sdk/unified-api/#hints).
	 */
	public open val hints: MutableStringKeyedMap<Any> = mutableMapOf()

	/**
	 * Map of tags to provide to the [Scope]. Supports the typed key syntax used by [processMap].
	 *
	 * For more information, see [the Sentry docs](https://develop.sentry.dev/sdk/unified-api/#terminology).
	 */
	public open val tags: MutableStringKeyedMap<String> = mutableMapOf()

	/**
	 * A function used internally to apply the data within this Sentry capture to a given Scope object.
	 *
	 * This function is in charge of applying the data stored within this class, and filtering it as required by
	 * the [allowedTypes] property.
	 */
	public open fun apply(scope: IScope) {
		scope.level = level
		scope.transaction = transaction

		if (allowedTypes.channels && channel != null) {
			logger.debug { "Adding context for channel ${channel?.id}, as it's allowed by the SentryDataTypeBuilder." }

			scope.setContexts(
				"channel",

				mutableMapOf(
					"id" to channel!!.id.toString(),
					"name" to channel!!.sentryName
				)
			)
		}

		if (allowedTypes.guilds && guild != null) {
			logger.debug { "Adding context for guild ${guild?.id}, as it's allowed by the SentryDataTypeBuilder." }

			scope.setContexts(
				"guild",

				mapOf(
					"id" to guild!!.id.toString(),
					"name" to guild!!.name,
				)
			)
		}

		if (allowedTypes.roles && role != null) {
			logger.debug { "Adding context for role ${role?.id}, as it's allowed by the SentryDataTypeBuilder." }

			scope.setContexts(
				"role",

				mutableMapOf(
					"id" to role!!.id.toString(),
					"name" to role!!.name
				)
			)
		}

		if (allowedTypes.users && user != null) {
			logger.debug { "Adding context for user ${user?.id}, as it's allowed by the SentryDataTypeBuilder." }

			scope.user(user!!)
		}

		if (allowedTypes.omittedData != null) {
			scope.setContexts("omitted_data_types", allowedTypes.omittedData!!)
		}

		processMap(tags).forEach(scope::setTag)
	}
}
