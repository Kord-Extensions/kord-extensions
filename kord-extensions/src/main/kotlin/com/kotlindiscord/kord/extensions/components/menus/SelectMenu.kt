/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.ComponentWithAction
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.sentry.BreadcrumbType
import com.kotlindiscord.kord.extensions.sentry.tag
import com.kotlindiscord.kord.extensions.sentry.user
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import io.sentry.Sentry
import mu.KLogger
import mu.KotlinLogging

/** Maximum number of options for a menu. **/
public const val OPTIONS_MAX: Int = 25

/** Maximum length for a menu's placeholder. **/
public const val PLACEHOLDER_MAX: Int = 100

/** Abstract class representing a select (dropdown) menu component. **/
public abstract class SelectMenu<C : SelectMenuContext, M : ModalForm>(
    timeoutTask: Task?,
) : ComponentWithAction<SelectMenuInteractionCreateEvent, C, M>(timeoutTask) {
    internal val logger: KLogger = KotlinLogging.logger {}

    /** The minimum number of choices that the user must make. **/
    public var minimumChoices: Int = 1

    /** The maximum number of choices that the user can make. Set to `null` for no maximum. **/
    public var maximumChoices: Int? = 1

    /** Placeholder text to show before the user has selected any options. **/
    public var placeholder: String? = null

    @Suppress("MagicNumber")  // WHY DO YOU THINK I ASSIGN IT HERE
    override val unitWidth: Int = 5

    /** Whether this select menu is disabled. **/
    public open var disabled: Boolean? = null

    /** Mark this select menu as disabled. **/
    public open fun disable() {
        disabled = true
    }

    /** Mark this select menu as enabled. **/
    public open fun enable() {
        disabled = null  // Don't ask me why this is
    }

    /** If enabled, adds the initial Sentry breadcrumb to the given context. **/
    public open suspend fun firstSentryBreadcrumb(context: C, button: SelectMenu<*, *>) {
        if (sentry.enabled) {
            context.sentry.breadcrumb(BreadcrumbType.User) {
                category = "component.selectMenu"
                message = "Select menu \"${button.id}\" submitted."

                data["component"] = button.id
                context.addContextDataToBreadcrumb(this)
            }
        }
    }

    /** A general way to handle errors thrown during the course of a select menu action's execution. **/
    public open suspend fun handleError(context: C, t: Throwable, button: SelectMenu<*, *>) {
        logger.error(t) { "Error during execution of select menu (${button.id}) action (${context.event})" }

        if (sentry.enabled) {
            logger.trace { "Submitting error to sentry." }

            val channel = context.channel
            val author = context.user.asUserOrNull()

            val sentryId = context.sentry.captureException(t) {
                if (author != null) {
                    user(author)
                }

                tag("private", "false")

                if (channel is DmChannel) {
                    tag("private", "true")
                }

                tag("component", button.id)

                Sentry.captureException(t)
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

    @Suppress("UnnecessaryParentheses")
    override fun validate() {
        super.validate()

        if ((this.placeholder?.length ?: 0) > PLACEHOLDER_MAX) {
            error("Menu components must not have a placeholder longer than $PLACEHOLDER_MAX characters.")
        }
    }

    /** Override this to implement a way to respond to the user, regardless of whatever happens. **/
    public abstract suspend fun respondText(context: C, message: String, failureType: FailureReason<*>)
}
