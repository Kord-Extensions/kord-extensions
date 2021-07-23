package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.InvalidEventHandlerException
import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.utils.getKoin
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.Event
import io.sentry.Sentry
import kotlinx.coroutines.Job
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.Serializable
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
 * @param type A [KClass] representing the event type this handler is subscribed to. This is for internal use.
 */
public open class EventHandler<T : Event>(
    public val extension: Extension,
    public val type: KClass<*>
) : KoinComponent {
    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

    /**
     * @suppress
     */
    public lateinit var body: suspend EventContext<T>.() -> Unit

    /**
     * @suppress
     */
    public val checkList: MutableList<Check<T>> = mutableListOf()

    /**
     * @suppress This is the job returned by `Kord#on`, which we cancel to stop listening.
     */
    public var job: Job? = null

    /** Cached locale variable, stored and retrieved by [getLocale]. **/
    public var resolvedLocale: Locale? = null

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
    public fun check(vararg checks: Check<T>): Unit = checks.forEach { checkList.add(it) }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this event handler.
     */
    public fun check(check: Check<T>): Boolean = checkList.add(check)

    /**
     * Define a simple Boolean check which must pass for the event handler to be executed.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     *
     * An event handler may have multiple checks - all checks must pass for the command to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this event handler.
     */
    public open fun booleanCheck(vararg checks: suspend (T) -> Boolean) {
        checks.forEach(::booleanCheck)
    }

    /**
     * Overloaded simple Boolean check function to allow for DSL syntax.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     *
     * @param check Check to apply to this event handler.
     */
    public open fun booleanCheck(check: suspend (T) -> Boolean) {
        check {
            if (check(event)) {
                pass()
            } else {
                fail()
            }
        }
    }

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
        for (check in checkList) {
            val context = CheckContext(event, defaultLocale)

            check(context)

            if (!context.passed) {
                return  // We don't send responses to plain event handlers
            }
        }

        val context = EventContext(this, event)
        val eventName = event::class.simpleName

        val firstBreadcrumb = if (sentry.enabled) {
            val data = mutableMapOf<String, Serializable>()

            val channelId = channelIdFor(event)
            val guildBehavior = guildFor(event)
            val messageBehavior = messageFor(event)
            val roleBehavior = roleFor(event)
            val userBehavior = userFor(event)

            if (channelId != null) {
                val channel = kord.getChannel(Snowflake(channelId))

                if (channel != null) {
                    data["channel"] = when (channel) {
                        is DmChannel -> "Private Message (${channel.id.asString})"
                        is GuildMessageChannel -> "#${channel.name} (${channel.id.asString})"

                        else -> channel.id.asString
                    }
                } else {
                    data["channel"] = channelId
                }
            }

            if (guildBehavior != null) {
                val guild = guildBehavior.asGuildOrNull()

                data["guild"] = if (guild != null) {
                    "${guild.name} (${guild.id.asString})"
                } else {
                    guildBehavior.id.asString
                }
            }

            if (messageBehavior != null) {
                data["message"] = messageBehavior.id.asString
            }

            if (roleBehavior != null) {
                val role = roleBehavior.guild.getRoleOrNull(roleBehavior.id)

                data["role"] = if (role != null) {
                    "@${role.name} (${role.id.asString})"
                } else {
                    roleBehavior.id.asString
                }
            }

            if (userBehavior != null) {
                val user = userBehavior.asUserOrNull()

                data["user"] = if (user != null) {
                    "${user.tag} (${user.id.asString})"
                } else {
                    userBehavior.id.asString
                }
            }

            sentry.createBreadcrumb(
                category = "event",
                type = "info",
                message = "Event \"$eventName\" fired.",
                data = data
            )
        } else {
            null
        }

        @Suppress("TooGenericExceptionCaught")  // Anything could happen here
        try {
            this.body(context)
        } catch (t: Throwable) {
            if (sentry.enabled && extension.bot.extensions.containsKey("sentry")) {
                logger.debug { "Submitting error to sentry." }

                Sentry.withScope {
                    it.tag("event", eventName ?: "Unknown")
                    it.tag("extension", extension.name)

                    it.addBreadcrumb(firstBreadcrumb!!)

                    context.breadcrumbs.forEach { breadcrumb -> it.addBreadcrumb(breadcrumb) }

                    val sentryId = Sentry.captureException(t, "Event processing failed.")

                    logger.debug { "Error submitted to Sentry: $sentryId" }
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
            val result = resolver(guild, channel, user)

            if (result != null) {
                locale = result
                break
            }
        }

        resolvedLocale = locale ?: extension.bot.settings.i18nBuilder.defaultLocale

        return resolvedLocale!!
    }

    /**
     * Given a translation key and bundle name, return the translation for the locale provided by the bot's configured
     * locale resolvers.
     */
    public suspend fun Event.translate(
        key: String,
        bundleName: String?,
        replacements: Array<Any?> = arrayOf()
    ): String {
        val locale = getLocale()

        return translationsProvider.translate(key, locale, bundleName, replacements)
    }

    /**
     * Given a translation key and possible replacements,return the translation for the given locale in the
     * extension's configured bundle, for the locale provided by the bot's configured locale resolvers.
     */
    public suspend fun Event.translate(key: String, replacements: Array<Any?> = arrayOf()): String = translate(
        key,
        extension.bundle,
        replacements
    )
}
