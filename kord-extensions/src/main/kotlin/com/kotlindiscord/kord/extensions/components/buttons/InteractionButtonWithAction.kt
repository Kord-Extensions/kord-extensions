/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.ComponentWithAction
import com.kotlindiscord.kord.extensions.components.types.HasPartialEmoji
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import io.sentry.Sentry
import mu.KLogger
import mu.KotlinLogging

/** Abstract class representing a button component that has a click action. **/
public abstract class InteractionButtonWithAction<C : InteractionButtonContext>(timeoutTask: Task?) :
    ComponentWithAction<ButtonInteractionCreateEvent, C>(timeoutTask), HasPartialEmoji {
    internal val logger: KLogger = KotlinLogging.logger {}

    /** Button label, for display on Discord. **/
    public var label: String? = null

    /** Whether this button is disabled. **/
    public open var disabled: Boolean = false

    public override var partialEmoji: DiscordPartialEmoji? = null

    /** Mark this button as disabled. **/
    public open fun disable() {
        disabled = true
    }

    /** Mark this button as enabled. **/
    public open fun enable() {
        disabled = false
    }

    /** If enabled, adds the initial Sentry breadcrumb to the given context. **/
    public open suspend fun firstSentryBreadcrumb(context: C, button: InteractionButtonWithAction<*>) {
        if (sentry.enabled) {
            context.sentry.breadcrumb(BreadcrumbType.User) {
                category = "component.button"
                message = "Button \"${button.id}\" clicked."

                val channel = context.channel.asChannelOrNull()
                val guild = context.guild?.asGuildOrNull()
                val message = context.message

                data["component"] = button.id

                if (channel != null) {
                    data["channel"] = when (channel) {
                        is DmChannel -> "Private Message (${channel.id})"
                        is GuildMessageChannel -> "#${channel.name} (${channel.id})"

                        else -> channel.id.toString()
                    }
                }

                if (guild != null) {
                    data["guild"] = "${guild.name} (${guild.id})"
                }

                if (message != null) {
                    data["message"] = message.id.toString()
                }
            }
        }
    }

    /** A general way to handle errors thrown during the course of a button action's execution. **/
    public open suspend fun handleError(context: C, t: Throwable, button: InteractionButtonWithAction<*>) {
        logger.error(t) { "Error during execution of button (${button.id}) action (${context.event})" }

        if (sentry.enabled) {
            logger.trace { "Submitting error to sentry." }

            val channel = context.channel
            val author = context.user.asUserOrNull()

            val sentryId = context.sentry.captureException(t, "Button action execution failed.") {
                if (author != null) {
                    user(author)
                }

                tag("private", "false")

                if (channel is DmChannel) {
                    tag("private", "true")
                }

                tag("component", button.id)

                Sentry.captureException(t, "Slash command execution failed.")
            }

            logger.info { "Error submitted to Sentry: $sentryId" }

            val errorMessage = if (bot.extensions.containsKey("sentry")) {
                context.translate("commands.error.user.sentry.slash", null, replacements = arrayOf(sentryId))
            } else {
                context.translate("commands.error.user", null)
            }

            respondText(context, errorMessage, FailureReason.ExecutionError(t))
        } else {
            respondText(
                context,
                context.translate("commands.error.user", null),
                FailureReason.ExecutionError(t)
            )
        }
    }

    override fun validate() {
        super.validate()

        if (label == null && partialEmoji == null) {
            error("Buttons must have either a label or emoji.")
        }
    }

    /** Override this to implement a way to respond to the user, regardless of whatever happens. **/
    public abstract suspend fun respondText(context: C, message: String, failureType: FailureReason<*>)
}
