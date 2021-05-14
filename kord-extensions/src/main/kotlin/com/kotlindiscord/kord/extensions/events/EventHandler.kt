package com.kotlindiscord.kord.extensions.events

import com.kotlindiscord.kord.extensions.InvalidEventHandlerException
import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.SentryAdapter
import com.kotlindiscord.kord.extensions.sentry.tag
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
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

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
public class EventHandler<T : Any>(public val extension: Extension, public val type: KClass<*>) : KoinComponent {
    /** Sentry adapter, for easy access to Sentry functions. **/
    public val sentry: SentryAdapter by inject()

    /** Kord instance, backing the ExtensibleBot. **/
    public val kord: Kord by inject()

    /**
     * @suppress
     */
    public lateinit var body: suspend EventContext<T>.() -> Unit

    /**
     * @suppress
     */
    public val checkList: MutableList<suspend (T) -> Boolean> = mutableListOf()

    /**
     * @suppress This is the job returned by `Kord#on`, which we cancel to stop listening.
     */
    public var job: Job? = null

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
     * A event handler may have multiple checks - all checks must pass for the event handler to be executed.
     * Checks will be run in the order that they're defined.
     *
     * This function can be used DSL-style with a given body, or it can be passed one or more
     * predefined functions. See the samples for more information.
     *
     * @param checks Checks to apply to this event handler.
     */
    public fun check(vararg checks: suspend (T) -> Boolean): Unit = checks.forEach { checkList.add(it) }

    /**
     * Overloaded check function to allow for DSL syntax.
     *
     * @param check Check to apply to this event handler.
     */
    public fun check(check: suspend (T) -> Boolean): Boolean = checkList.add(check) // endregion

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
            if (!check.invoke(event)) {
                return
            }
        }

        val context = EventContext(this, event)
        val eventName = event::class.simpleName

        val firstBreadcrumb = if (sentry.enabled) {
            if (event is Event) {
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
}
