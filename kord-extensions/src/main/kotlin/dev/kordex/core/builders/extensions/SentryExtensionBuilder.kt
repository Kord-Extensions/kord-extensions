/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders.extensions

import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.sentry.captures.SentryCapture
import io.sentry.SentryLevel

internal typealias SentryDataTypeBuilder =
	SentryExtensionBuilder.SentryExtensionDataTypeBuilder

internal typealias SentryDataTypeTransformer =
	suspend (SentryDataTypeBuilder).(SentryCapture) -> Unit

/** Builder used to configure Sentry and the Sentry extension. **/
@BotBuilderDSL
public open class SentryExtensionBuilder {
	/** Whether to enable Sentry integration. This includes the extension, and [SentryAdapter] setup. **/
	public open var enable: Boolean = false

	/**
	 * Whether to enable the Sentry extension, which provides feedback commands.
	 *
	 * This will be ignored if [enable] is `false`.
	 */
	public open var feedbackExtension: Boolean = false

	/** Whether to enable Sentry's debug mode. **/
	public open var debug: Boolean = false

	/** Your Sentry DSN, required for submitting events to Sentry. **/
	public open var dsn: String? = null

	/** Optional distribution name to send to Sentry. **/
	public open var distribution: String? = null

	/** Optional environment name to send to Sentry. **/
	public open var environment: String? = null

	/** Optional release version to send to Sentry. **/
	public open var release: String? = null

	/** Optional server name to send to Sentry. **/
	public open var serverName: String? = null

	/** Whether to ping users when responding to them. **/
	public var pingInReply: Boolean = true

	/** How many events to send to Sentry, as a percentage. Defaults to 1.0, meaning all events. **/
	public var sampleRate: Double = 1.0

	/** Builder used to construct a [SentryAdapter] instance, usually the constructor. **/
	public open var builder: () -> SentryAdapter = ::SentryAdapter

	/**
	 * Function in charge of setting up the [SentryAdapter], by calling its `setup` function. You can use this
	 * if you need to pass extra parameters to the setup function, but make sure you pass everything that's
	 * required.
	 */
	public open var setupCallback: SentryAdapter.() -> Unit = {
		this.init { options ->
			options.dsn = dsn
			options.isDebug = debug

			options.dist = distribution
			options.environment = environment
			options.release = release
			options.serverName = serverName

			options.isAttachThreads = false
			options.sampleRate = sampleRate

			options.setDiagnosticLevel(SentryLevel.WARNING)
		}
	}

	/** @suppress Storage for Sentry datatype settings container. **/
	public val dataTypeBuilder: SentryExtensionDataTypeBuilder = SentryExtensionDataTypeBuilder()

	/** @suppress Storage for Sentry datatype transformers. **/
	public val dataTypeTransformers: MutableList<SentryDataTypeTransformer> = mutableListOf()

	/** @suppress Storage for Sentry submission predicates. **/
	public val predicates: MutableList<suspend SentryCapture.() -> Boolean> = mutableListOf()

	/**
	 * Register a Sentry submission predicate.
	 *
	 * You can use a submission predicate to filter which captures Kord Extensions sends to Sentry.
	 * Return `false` from your predicate to prevent the capture from being sent.
	 *
	 * The Sentry adapter will iterate the predicates in order of definition, stopping when one returns `false`
	 * or there's none left.
	 */
	public fun predicate(body: suspend SentryCapture.() -> Boolean) {
		predicates.add(body)
	}

	/**
	 * Configure the default data types sent via (or omitted from) Sentry captures.
	 */
	public fun defaultDataTypes(body: SentryExtensionDataTypeBuilder.() -> Unit) {
		body(dataTypeBuilder)
	}

	/**
	 * Registry a Sentry data type transformer.
	 *
	 * This allows you to dynamically change which data types are sent to Sentry, depending on the given capture
	 * object.
	 *
	 * Transformers may modify a clone of the default [SentryExtensionDataTypeBuilder] freely, which will be
	 * based on the one configured via [defaultDataTypes].
	 */
	public fun dataTypeTransformer(body: SentryDataTypeTransformer) {
		dataTypeTransformers.add(body)
	}

	/** Register a builder used to construct a [SentryAdapter] instance, usually the constructor. **/
	public fun builder(body: () -> SentryAdapter) {
		builder = body
	}

	/**
	 * Convenience function to enable and set the DSN, but only if the supplied DSN isn't null.
	 *
	 * Intended for use with `envOrNull`.
	 */
	public fun enableIfDSN(sentryDSN: String?) {
		if (sentryDSN != null) {
			dsn = sentryDSN
			enable = true
		}
	}

	/**
	 * Register the function in charge of setting up the [SentryAdapter], by calling its `setup` function.
	 * You can use this if you need to pass extra parameters to the setup function, but make sure you pass
	 * everything that's required.
	 */
	public fun setup(body: SentryAdapter.() -> Unit) {
		setupCallback = body
	}

	public class SentryExtensionDataTypeBuilder {
		/** Whether to send command arguments to Sentry. **/
		public var arguments: Boolean = true

		/** Whether to send channel information to Sentry. **/
		public var channels: Boolean = true

		/** Whether to send guild information to Sentry. **/
		public var guilds: Boolean = true

		/** Whether to send guild information to Sentry. **/
		public var roles: Boolean = true

		/** Whether to send user information to Sentry. **/
		public var users: Boolean = true

		/** Comma-separated list of omitted data types, based on this object's configured booleans. **/
		public val omittedData: String? by lazy {
			val omitted: MutableList<String> = mutableListOf()

			if (!arguments) {
				omitted.add("arguments")
			}

			if (!channels) {
				omitted.add("channels")
			}

			if (!guilds) {
				omitted.add("guilds")
			}

			if (!roles) {
				omitted.add("roles")
			}

			if (!users) {
				omitted.add("users")
			}

			if (omitted.isEmpty()) {
				null
			} else {
				omitted.joinToString()
			}
		}

		/** Clone this object, allowing for just-in-time customisation where needed. **/
		public fun clone(): SentryExtensionDataTypeBuilder {
			val other = SentryExtensionDataTypeBuilder()

			other.arguments = this.arguments
			other.channels = this.channels
			other.guilds = this.guilds
			other.users = this.users

			return other
		}
	}
}
