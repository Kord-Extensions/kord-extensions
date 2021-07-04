@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.ComponentCheckFun
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.contexts.ActionableComponentContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.utils.ackEphemeral
import com.kotlindiscord.kord.extensions.utils.ackPublic
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KotlinLogging
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

    /** Unique ID for this button. Required by Discord. **/
    public open val id: String = UUID.randomUUID().toString()

    /**
     * Automatic ack type, if not following a parent context.
     */
    public open var ackType: AutoAckType? = AutoAckType.EPHEMERAL

    /** Whether to send a deferred acknowledgement instead of a normal one. **/
    public open var deferredAck: Boolean = false

    /** Whether to follow the ack type of the parent slash command context, if any. **/
    public open var followParent: Boolean = true

    /** @suppress Internal variable, a list of checks to apply to click actions. **/
    public open val checks: MutableList<ComponentCheckFun> = mutableListOf()

    /** @suppress Internal variable, the click action to run. **/
    public open lateinit var body: suspend R.() -> Unit

    public override fun validate() {
        if (!this::body.isInitialized) {
            error("Actionable components must have an action defined.")
        }
    }

    /** Register a check that must pass for this button to be actioned. Failing checks will fail quietly. **/
    public open fun check(vararg checks: ComponentCheckFun): Boolean =
        this.checks.addAll(checks)

    /** Register a check that must pass for this button to be actioned. Failing checks will fail quietly. **/
    public open fun check(check: ComponentCheckFun): Boolean =
        checks.add(check)

    /** Register the click action that should be run when this button is clicked, assuming the checks pass. **/
    public open fun action(action: suspend R.() -> Unit) {
        this.body = action
    }

    override suspend fun call(
        components: Components,
        extension: Extension,
        event: InteractionCreateEvent,
        parentContext: SlashCommandContext<*>?
    ) {
        if (!checks.all { it(event) }) {
            (event.interaction as T).acknowledgeEphemeralDeferredMessageUpdate()
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
            when (ackType) {
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
        event: InteractionCreateEvent,
        components: Components,
        interactionResponse: InteractionResponseBehavior? = null,
        interaction: T = event.interaction as T
    ): R
}
