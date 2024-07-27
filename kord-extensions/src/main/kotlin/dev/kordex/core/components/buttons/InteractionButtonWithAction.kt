/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.buttons

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kordex.core.components.ComponentWithAction
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.types.HasPartialEmoji
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.scheduling.Task
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/** Abstract class representing a button component that has a click action. **/
public abstract class InteractionButtonWithAction<C : InteractionButtonContext, M : ModalForm>(timeoutTask: Task?) :
	ComponentWithAction<ButtonInteractionCreateEvent, C, M>(timeoutTask), HasPartialEmoji {
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
	public open suspend fun firstSentryBreadcrumb(context: C, button: InteractionButtonWithAction<*, *>) {
		if (sentry.enabled) {
			context.sentry.context(
				"component",

				mapOf(
					"id" to button.id,
					"type" to "button"
				)
			)

			context.sentry.breadcrumb(BreadcrumbType.User) {
				category = "component.button"
				message = "Button \"${button.id}\" clicked."

				data["component.id"] = button.id

				context.addContextDataToBreadcrumb(this)
			}
		}
	}

	/** A general way to handle errors thrown during the course of a button action's execution. **/
	@Suppress("StringLiteralDuplication")
	public open suspend fun handleError(context: C, t: Throwable, button: InteractionButtonWithAction<*, *>) {
		logger.error(t) { "Error during execution of button (${button.id}) action (${context.event})" }

		if (sentry.enabled) {
			logger.trace { "Submitting error to sentry." }

			val sentryId = context.sentry.captureThrowable(t) {
				channel = context.channel.asChannelOrNull()
				user = context.user.asUserOrNull()
			}

			val errorMessage = if (sentryId != null) {
				logger.info { "Error submitted to Sentry: $sentryId" }

				if (bot.extensions.containsKey("sentry")) {
					context.translate("commands.error.user.sentry.slash", null, replacements = arrayOf(sentryId))
				} else {
					context.translate("commands.error.user", null)
				}
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
