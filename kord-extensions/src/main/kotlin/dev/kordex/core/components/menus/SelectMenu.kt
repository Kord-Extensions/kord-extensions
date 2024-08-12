/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus

import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.ComponentWithAction
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.impl.SENTRY_EXTENSION_NAME
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.scheduling.Task
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/** Maximum length for an option's description. **/
public const val DESCRIPTION_MAX: Int = 100

/** Maximum length for an option's label. **/
public const val LABEL_MAX: Int = 100

/** Maximum number of options for a menu. **/
public const val OPTIONS_MAX: Int = 25

/** Maximum length for a menu's placeholder. **/
public const val PLACEHOLDER_MAX: Int = 100

/** Maximum length for an option's value. **/
public const val VALUE_MAX: Int = 100

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
	public open suspend fun firstSentryBreadcrumb(context: C, component: SelectMenu<*, *>) {
		if (sentry.enabled) {
			context.sentry.context(
				"component",

				mapOf(
					"id" to component.id,
					"type" to "select-menu"
				)
			)

			context.sentry.breadcrumb(BreadcrumbType.User) {
				category = "component.selectMenu"
				message = "Select menu \"${component.id}\" submitted."

				data["component.id"] = component.id

				context.addContextDataToBreadcrumb(this)
			}
		}
	}

	/** A general way to handle errors thrown during the course of a select menu action's execution. **/
	@Suppress("StringLiteralDuplication")
	public open suspend fun handleError(context: C, t: Throwable, button: SelectMenu<*, *>) {
		logger.error(t) { "Error during execution of select menu (${button.id}) action (${context.event})" }

		if (sentry.enabled) {
			logger.trace { "Submitting error to sentry." }

			val sentryId = context.sentry.captureThrowable(t) {
				channel = context.channel.asChannelOrNull()
				user = context.user.asUserOrNull()
			}

			val errorMessage = if (sentryId != null) {
				logger.info { "Error submitted to Sentry: $sentryId" }

				if (bot.extensions.containsKey(SENTRY_EXTENSION_NAME)) {
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
