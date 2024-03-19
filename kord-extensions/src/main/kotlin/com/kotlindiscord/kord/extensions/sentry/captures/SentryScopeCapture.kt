/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.sentry.captures

import com.kotlindiscord.kord.extensions.sentry.sentryName
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
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

	/** Set the given [value] into the [tags] map under [key]. **/
	@Deprecated(
		"Direct access to the Scope object is no longer provided. Access the tags map directly instead.",
		replaceWith = ReplaceWith("tags.set(key, value)"),
		level = DeprecationLevel.ERROR
	)
	public open fun tag(key: String, value: String): Unit =
		tags.set(key, value)

	/** Set the given [value] into the [tags] map under [key]. **/
	@Deprecated(
		"Direct access to the Scope object is no longer provided. Access the tags map directly instead.",
		replaceWith = ReplaceWith("tags.set(key, value)"),
		level = DeprecationLevel.ERROR
	)
	public open fun setTag(key: String, value: String): Unit =
		tags.set(key, value)

	/** Get the current value from the [tags] map under [key], or `null` if it doesn't exist. **/
	@Deprecated(
		"Direct access to the Scope object is no longer provided. Access the tags map directly instead.",
		replaceWith = ReplaceWith("tags.get(key)"),
		level = DeprecationLevel.ERROR
	)
	public open fun getTag(key: String): Any? =
		tags[key]

	/** Remove the value from the [tags] map under [key], returning it or `null` if it doesn't exist. **/
	@Deprecated(
		"Direct access to the Scope object is no longer provided. Access the tags map directly instead.",
		replaceWith = ReplaceWith("tags.remove(key)"),
		level = DeprecationLevel.ERROR
	)
	public open fun removeTag(key: String): Any? =
		tags.remove(key)
}
