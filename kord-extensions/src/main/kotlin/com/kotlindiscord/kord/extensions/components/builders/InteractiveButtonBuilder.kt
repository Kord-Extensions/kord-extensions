@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.checks.channelFor
import com.kotlindiscord.kord.extensions.checks.guildFor
import com.kotlindiscord.kord.extensions.checks.userFor
import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.ButtonCheckFun
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.InteractiveButtonAction
import com.kotlindiscord.kord.extensions.components.contexts.InteractiveButtonContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import io.sentry.Sentry
import io.sentry.protocol.SentryId
import mu.KotlinLogging
import java.io.Serializable
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Button builder representing an interactive button, with click action.
 *
 * Either a [label] or [emoji] must be provided. [style] must not be [ButtonStyle.Link].
 */
public open class InteractiveButtonBuilder : ButtonBuilder() {
    /** Unique ID for this button. Required by Discord. **/
    public open val id: String = UUID.randomUUID().toString()

    /** Button style. **/
    public open var style: ButtonStyle = ButtonStyle.Primary

    /**
     * Automatic ack type, if not following a parent context.
     */
    public open var ackType: AutoAckType? = AutoAckType.EPHEMERAL

    /** @suppress Internal variable, a list of checks to apply to click actions. **/
    public open val checks: MutableList<ButtonCheckFun> = mutableListOf()

    /** @suppress Internal variable, the click action to run. **/
    public open lateinit var body: InteractiveButtonAction

    public override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji
            label = this@InteractiveButtonBuilder.label
        }
    }

    public override fun validate() {
        if (label == null && partialEmoji == null) {
            error("Interactive buttons must have either a label or emoji.")
        }

        if (style == ButtonStyle.Link) {
            error("The Link button style is reserved for link buttons.")
        }

        if (!this::body.isInitialized) {
            error("Interactive buttons must have an action defined.")
        }
    }

    /** Register a check that must pass for this button to be actioned. Failing checks will show an error. **/
    public open fun check(vararg checks: ButtonCheckFun): Boolean = this.checks.addAll(checks)

    /** Register a check that must pass for this button to be actioned. Failing checks will show an error. **/
    public open fun check(check: ButtonCheckFun): Boolean = checks.add(check)

    /** Register the click action that should be run when this button is clicked, assuming the checks pass. **/
    public open fun action(action: InteractiveButtonAction) {
        this.body = action
    }

    override suspend fun call(
        components: Components,
        extension: Extension,
        event: InteractionCreateEvent,
        parentContext: SlashCommandContext<*>?
    ) {
        if (!checks.all { it(event) }) {
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

        val interaction = event.interaction as ComponentInteraction

        val response = if (parentContext != null) {
            when (parentContext.isEphemeral) {
                true -> interaction.acknowledgeEphemeral()
                false -> interaction.acknowledgePublic()

                else -> null
            }
        } else {
            when (ackType) {
                AutoAckType.EPHEMERAL -> interaction.acknowledgeEphemeral()
                AutoAckType.PUBLIC -> interaction.acknowledgePublic()

                else -> null
            }
        }

        val context = InteractiveButtonContext(
            extension, event, components, response, interaction
        )

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
}
