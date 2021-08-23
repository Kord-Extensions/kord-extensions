@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.checks.*
import com.kotlindiscord.kord.extensions.checks.types.Check
import com.kotlindiscord.kord.extensions.checks.types.CheckContext
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.contexts.ActionableComponentContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.ackEphemeral
import com.kotlindiscord.kord.extensions.utils.ackPublic
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KotlinLogging
import org.koin.core.component.inject
import java.io.Serializable
import java.util.*

/**
 * Button builder representing an interactive button, with click action.
 *
 * Either a [label] or [emoji] must be provided. [style] must not be [ButtonStyle.Link].
 */
public abstract class ActionableComponentBuilder<T : ComponentInteraction, R : ActionableComponentContext<T>> :
    ComponentBuilder() {
    private val logger = KotlinLogging.logger {}
    private val translations: TranslationsProvider by inject()

    /** Unique ID for this button. Required by Discord. **/
    public open var id: String = UUID.randomUUID().toString()

    /**
     * Automatic ack type, if not following a parent context.
     */
    public open var autoAck: AutoAckType? = AutoAckType.EPHEMERAL

    /**
     * Automatic ack type, if not following a parent context.
     */
    @Deprecated(
        "This property was renamed to autoAck for consistency.",
        ReplaceWith("autoAck"),
        DeprecationLevel.ERROR
    )
    public open var ackType: AutoAckType?
        get() = autoAck
        set(value) {
            autoAck = value
        }

    /** Whether to send a deferred acknowledgement instead of a normal one. **/
    public open var deferredAck: Boolean = false

    /** Whether to follow the ack type of the parent slash command context, if any. **/
    public open var followParent: Boolean = true

    /** @suppress Internal variable, a list of checks to apply to click actions. **/
    public open val checks: MutableList<Check<ComponentInteractionCreateEvent>> = mutableListOf()

    /** @suppress Internal variable, the click action to run. **/
    public open lateinit var body: suspend R.() -> Unit

    public override fun validate() {
        if (!this::body.isInitialized) {
            error("Actionable components must have an action defined.")
        }
    }

    /** Register a check that must pass for this button to be actioned. **/
    public open fun check(vararg checks: Check<ComponentInteractionCreateEvent>): Boolean =
        this.checks.addAll(checks)

    /** Register a check that must pass for this button to be actioned. **/
    public open fun check(check: Check<ComponentInteractionCreateEvent>): Boolean =
        checks.add(check)

    /**
     * Define a simple Boolean check which must pass for the button to be actioned.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     */
    public open fun booleanCheck(vararg checks: suspend (ComponentInteractionCreateEvent) -> Boolean) {
        checks.forEach(::booleanCheck)
    }

    /**
     * Overloaded simple Boolean check function to allow for DSL syntax.
     *
     * Boolean checks are simple wrappers around the regular check system, allowing you to define a basic check that
     * takes an event object and returns a [Boolean] representing whether it passed. This style of check does not have
     * the same functionality as a regular check, and cannot return a message.
     */
    public open fun booleanCheck(check: suspend (ComponentInteractionCreateEvent) -> Boolean) {
        check {
            if (check(event)) {
                pass()
            } else {
                fail()
            }
        }
    }

    /** Register the click action that should be run when this button is clicked, assuming the checks pass. **/
    public open fun action(action: suspend R.() -> Unit) {
        this.body = action
    }

    /** Run this component's checks, returning a Boolean representing whether the checks passed. **/
    public open suspend fun runChecks(event: ComponentInteractionCreateEvent, sendMessage: Boolean = true): Boolean {
        val interaction = event.interaction as T
        val locale = event.getLocale()

        for (check in checks) {
            val context = CheckContext(event, locale)

            check(context)

            if (!context.passed) {
                val message = context.message

                if (message != null && sendMessage) {
                    interaction.respondEphemeral {
                        content = translations.translate(
                            "checks.responseTemplate",
                            replacements = arrayOf(message)
                        )
                    }
                } else {
                    interaction.acknowledgeEphemeralDeferredMessageUpdate()
                }

                return false
            }
        }

        return true
    }

    override suspend fun call(
        components: Components,
        extension: Extension,
        event: ComponentInteractionCreateEvent,
        parentContext: SlashCommandContext<*>?
    ) {
        if (!runChecks(event)) {
            return
        }

        val firstBreadcrumb = if (sentry.enabled) {
            val channel = channelFor(event)
            val guild = guildFor(event)?.asGuildOrNull()

            val data = mutableMapOf<String, Serializable>()

            if (channel != null) {
                data["channel"] = when (channel) {
                    is DmChannel -> "Private Message (${channel.id.asString})"
                    is GuildMessageChannel -> "#${channel.name} (${channel.id.asString})"

                    else -> channel.id.asString
                }
            }

            if (guild != null) {
                data["guild"] = "${guild.name} (${guild.id.asString})"
            }

            sentry.createBreadcrumb(
                category = "interaction",
                type = "user",
                message = "Interaction for component \"$id\" received.",
                data = data
            )
        } else {
            null
        }

        val interaction = event.interaction as T

        val response = if (parentContext != null && followParent) {
            when (parentContext.isEphemeral) {
                true -> interaction.ackEphemeral(deferredAck)
                false -> interaction.ackPublic(deferredAck)

                else -> null
            }
        } else {
            when (autoAck) {
                AutoAckType.EPHEMERAL -> interaction.ackEphemeral(deferredAck)
                AutoAckType.PUBLIC -> interaction.ackPublic(deferredAck)

                else -> null
            }
        }

        val context = getContext(
            extension, event, components, response, interaction
        )

        context.populate()

        @Suppress("TooGenericExceptionCaught")
        try {
            body(context)
        } catch (e: CommandException) {
            context.respond(e.toString())
        } catch (t: Throwable) {
            if (sentry.enabled) {
                logger.debug { "Submitting error to sentry." }

                lateinit var sentryId: SentryId

                val user = userFor(event)?.asUserOrNull()
                val channel = channelFor(event)?.asChannelOrNull()

                Sentry.withScope {
                    if (user != null) {
                        it.user(user)
                    }

                    it.tag("private", "false")

                    if (channel is DmChannel) {
                        it.tag("private", "true")
                    }

                    it.tag("extension", extension.name)
                    it.addBreadcrumb(firstBreadcrumb!!)

                    context.breadcrumbs.forEach(it::addBreadcrumb)

                    sentryId = Sentry.captureException(t, "Component interaction execution failed.")

                    logger.debug { "Error submitted to Sentry: $sentryId" }
                }

                sentry.addEventId(sentryId)

                logger.error(t) { "Error thrown during component interaction" }

                if (extension.bot.extensions.containsKey("sentry")) {
                    context.respond(
                        context.translate(
                            "commands.error.user.sentry.slash",
                            null,
                            replacements = arrayOf(sentryId)
                        )
                    )
                } else {
                    context.respond(
                        context.translate("commands.error.user")
                    )
                }
            } else {
                logger.error(t) { "Error thrown during component interaction" }

                context.respond(context.translate("commands.error.user", null))
            }
        }
    }

    /** Function to be overridden in order to retrieve a context object of the correct type. **/
    public abstract fun getContext(
        extension: Extension,
        event: ComponentInteractionCreateEvent,
        components: Components,
        interactionResponse: InteractionResponseBehavior? = null,
        interaction: T = event.interaction as T
    ): R
}
