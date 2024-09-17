/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events

import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kordex.core.InvalidEventHandlerException
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.checks.*
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.checks.types.CheckWithCache
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.TranslationsProvider
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getKoin
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import org.koin.core.component.inject
import java.util.*
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

private val defaultLocale: Locale
	get() =
		getKoin().get<ExtensibleBotBuilder>().i18nBuilder.defaultLocale

/**
 * Class representing an event handler. Event handlers react to a given Kord event.
 *
 * You shouldn't need to use this class directly - instead, create an [Extension] and use the
 * [event function][Extension.event] to register your event handler, by overriding the [Extension.setup]
 * function.
 *
 * @param extension The [Extension] that registered this event handler.
 */
public open class EventHandler<T : Event>(
	public val extension: Extension,
) : KordExKoinComponent {
	/** Sentry adapter, for easy access to Sentry functions. **/
	public val sentry: SentryAdapter by inject()

	/** Current Kord instance powering the bot. **/
	public open val kord: Kord by inject()

	/** Translations provider, for retrieving translations. **/
	public val translationsProvider: TranslationsProvider by inject()

	/**
	 * @suppress
	 */
	public lateinit var body: suspend EventContext<T>.() -> Unit

	/** @suppress **/
	@InternalAPI
	public lateinit var type: KClass<T>

	/**
	 * @suppress
	 */
	public val checkList: MutableList<CheckWithCache<T>> = mutableListOf()

	/**
	 * @suppress This is the job returned by `Kord#on`, which we cancel to stop listening.
	 */
	public var job: Job? = null

	/** @suppress Internal hack to work around logic ordering with inline functions. **/
	public var listenerRegistrationCallable: (() -> Unit)? = null

	/** Cached locale variable, stored and retrieved by [getLocale]. **/
	public var resolvedLocale: Locale? = null

	/**
	 * Coroutine scope used to launch this event handler when handling a new event.
	 *
	 * Defaults to the current [Kord] instance.
	 */
	public var coroutineScope: CoroutineScope = kord

	/**
	 * An internal function used to ensure that all of an event handler's required arguments are present.
	 *
	 * @throws InvalidEventHandlerException Thrown when a required argument hasn't been set.
	 */
	@Throws(InvalidEventHandlerException::class)
	public fun validate() {
		if (!::body.isInitialized) {
			throw InvalidEventHandlerException("No event handler action given.")
		}
	}

	// region: DSL functions

	/**
	 * Define what will happen when your event handler is invoked.
	 *
	 * @param action The body of your event handler, which will be executed when it is invoked.
	 */
	public fun action(action: suspend EventContext<T>.() -> Unit) {
		this.body = action
	}

	/**
	 * Define a check which must pass for the event handler to be executed.
	 *
	 * An event handler may have multiple checks - all checks must pass for the event handler to be executed.
	 * Checks will be run in the order that they're defined.
	 *
	 * This function can be used DSL-style with a given body, or it can be passed one or more
	 * predefined functions. See the samples for more information.
	 *
	 * @param checks Checks to apply to this event handler.
	 */
	public fun check(vararg checks: CheckWithCache<T>): Unit = checks.forEach { checkList.add(it) }

	/**
	 * Overloaded check function to allow for DSL syntax.
	 *
	 * @param check Check to apply to this event handler.
	 */
	public fun check(check: CheckWithCache<T>): Boolean = checkList.add(check)

	// endregion

	/**
	 * Execute this event handler, given an event.
	 *
	 * This function takes an event of type T and executes the [event handler body][action], assuming all checks pass.
	 *
	 * If an exception is thrown by the [event handler body][action], it is caught and a traceback
	 * is printed.
	 *
	 * @param event The given event object.
	 */
	public suspend fun call(event: T) {
		val cache: MutableStringKeyedMap<Any> = mutableMapOf()

		for (check in checkList) {
			val context = CheckContextWithCache(event, defaultLocale, cache)

			check(context)

			if (!context.passed) {
				return  // We don't send responses to plain event handlers
			}
		}

		val context = EventContext(this, event, cache)
		val eventName = event::class.simpleName

		if (sentry.enabled) {
			context.sentry.context(
				"event",

				mapOf(
					"name" to (eventName ?: "Unknown"),
					"extension" to extension.name
				),
			)

			context.sentry.breadcrumb(BreadcrumbType.Info) {
				val messageBehavior = messageFor(event)
				val thread = threadFor(event)?.asChannel()
				val roleBehavior = roleFor(event)

				category = "event"
				message = "Event \"$eventName\" fired."

				channel = topChannelFor(event)?.asChannel()
				guild = guildFor(event)?.asGuildOrNull()
				role = roleBehavior?.guild?.getRoleOrNull(roleBehavior.id)
				user = userFor(event)?.asUserOrNull()

				if (messageBehavior != null) {
					data["message.id"] = messageBehavior.id.toString()
				}

				if (thread != null) {
					data["channel.thread.id"] = thread.id.toString()
					data["channel.thread.name"] = thread.name
				}
			}
		}

		@Suppress("TooGenericExceptionCaught")  // Anything could happen here
		try {
			this.body(context)
		} catch (t: Throwable) {
			if (sentry.enabled) {
				logger.trace { "Submitting error to sentry." }

				val sentryId = context.sentry.captureThrowable(t)

				if (sentryId != null) {
					logger.info { "Error submitted to Sentry: $sentryId" }
				}

				logger.error(t) { "Error during execution of event handler ($eventName)" }
			} else {
				logger.error(t) { "Error during execution of event handler ($eventName)" }
			}
		}
	}

	/** Resolve the locale for the given event. **/
	public suspend fun Event.getLocale(): Locale {
		var locale: Locale? = resolvedLocale

		if (locale != null) {
			return locale
		}

		val guild = guildFor(this)
		val channel = channelFor(this)
		val user = userFor(this)

		for (resolver in extension.bot.settings.i18nBuilder.localeResolvers) {
			val result = resolver(guild, channel, user, interactionFor(this))

			if (result != null) {
				locale = result
				break
			}
		}

		resolvedLocale = locale ?: extension.bot.settings.i18nBuilder.defaultLocale

		return resolvedLocale!!
	}

	/**
	 * Given a translation key, return the translation for the locale provided by the bot's configured
	 * locale resolvers.
	 */
	public suspend fun Event.translate(
		key: Key,
		replacements: Array<Any?> = arrayOf(),
	): String =
		key
			.withLocale(getLocale())
			.translateArray(replacements)

	/**
	 * Given a translation key, return the translation for the locale provided by the bot's configured
	 * locale resolvers.
	 */
	public suspend fun Event.translate(
		key: Key,
		replacements: Map<String, Any?>,
	): String =
		key
			.withLocale(getLocale())
			.translateNamed(replacements)
}
