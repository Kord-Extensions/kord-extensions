package com.kotlindiscord.kord.extensions.sentry.captures

import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.sentryName
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import io.sentry.Breadcrumb
import io.sentry.Scope
import io.sentry.SentryLevel

/**
 * Sentry capture type, used when creating [Breadcrumb]s to add to a Sentry scope for later submission.
 *
 * @param type Sentry breadcrumb type. Defaults to [BreadcrumbType.Default].
 */
public class SentryBreadcrumbCapture(
	public val type: BreadcrumbType = BreadcrumbType.Default,
) : SentryCapture() {

	/**
	 * Map of breadcrumb data to provide to the [Scope].
	 * Supports the typed key syntax used by [processMap].
	 * This is usually arbitrary data, but some [BreadcrumbType]s have special handling for specific keys.
	 *
	 * For more information, see [the Sentry docs](https://develop.sentry.dev/sdk/event-payloads/breadcrumbs/).
	 */
	public val data: MutableStringKeyedMap<Any> = mutableMapOf()

	/** Breadcrumb category. Arbitrary, but some categories change how Sentry renders the breadcrumb. **/
	public var category: String? = null

	/** Breadcrumb severity level. **/
	public var level: SentryLevel? = null

	/** Human-readable breadcrumb message. Displayed by Sentry with the whitespace preserved. **/
	public var message: String? = null

	/**
	 * A function used internally to apply the data within this Sentry capture to a given [Breadcrumb] object.
	 *
	 * This function is in charge of applying the data stored within this class, and filtering it as required by
	 * the [allowedTypes] property.
	 */
	public fun apply(breadcrumb: Breadcrumb) {
		breadcrumb.category = category
		breadcrumb.level = level
		breadcrumb.message = message
		breadcrumb.type = type.name

		populateData()
		addProcessedData(breadcrumb)

		val missingKeys = type.requiredKeys.filter { breadcrumb.getData(it) == null }

		if (missingKeys.isNotEmpty()) {
			logger.warn {
				"Ignoring breadcrumb type ${type.name}, as the following data is missing: ${missingKeys.joinToString()}"
			}

			breadcrumb.type = BreadcrumbType.Default.name
		}
	}

	private fun populateData() {
		if (allowedTypes.channels && channel != null) {
			data["channel.id"] = channel!!.id.toString()
			data["channel.name"] = channel!!.sentryName
		}

		if (allowedTypes.guilds && guild != null) {
			data["guild.id"] = guild!!.id.toString()
			data["guild.name"] = guild!!.name
		}

		if (allowedTypes.users && user != null) {
			data["user.id"] = user!!.id.toString()
			data["user.name"] = user!!.tag
		}

		if (allowedTypes.roles && role != null) {
			data["role.id"] = role!!.id.toString()
			data["role.name"] = role!!.name
		}

		if (allowedTypes.omittedData != null) {
			data["omitted"] = allowedTypes.omittedData!!
		}
	}

	private fun addProcessedData(breadcrumb: Breadcrumb) {
		processMap(data)
			.forEach(breadcrumb::setData)
	}

	/** Get the current value from the [data] map under [key], or `null` if it doesn't exist. **/
	@Deprecated(
		"Direct access to the Breadcrumb object is no longer provided. Access the data map directly instead.",
		replaceWith = ReplaceWith("data.get(key)"),
		level = DeprecationLevel.ERROR
	)
	public fun getData(key: String): Any? =
		data[key]

	/** Set the given [value] into the [data] map under [key]. **/
	@Deprecated(
		"Direct access to the Breadcrumb object is no longer provided. Access the data map directly instead.",
		replaceWith = ReplaceWith("data.set(key, value)"),
		level = DeprecationLevel.ERROR
	)
	public fun setData(key: String, value: Any): Unit =
		data.set(key, value)

	/** Remove the value from the [data] map under [key], returning it or `null` if it doesn't exist. **/
	@Deprecated(
		"Direct access to the Breadcrumb object is no longer provided. Access the data map directly instead.",
		replaceWith = ReplaceWith("data.remove(key)"),
		level = DeprecationLevel.ERROR
	)
	public fun removeData(key: String): Any? =
		data.remove(key)
}
