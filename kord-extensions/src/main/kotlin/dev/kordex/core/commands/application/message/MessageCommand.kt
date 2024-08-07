/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application.message

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.components.ComponentRegistry
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.impl.SENTRY_EXTENSION_NAME
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

/**
 * Message context command, for right-click actions on messages.
 * @param modal Callable returning a `ModalForm` object, if any
 */
public abstract class MessageCommand<C : MessageCommandContext<C, M>, M : ModalForm>(
	extension: Extension,
	public open val modal: (() -> M)? = null,
) : dev.kordex.core.commands.application.ApplicationCommand<MessageCommandInteractionCreateEvent>(extension) {
	private val logger: KLogger = KotlinLogging.logger {}

	/** @suppress This is only meant for use by code that extends the command system. **/
	public val componentRegistry: ComponentRegistry by inject()

	/** Command body, to be called when the command is executed. **/
	public lateinit var body: suspend C.(M?) -> Unit

	override val type: ApplicationCommandType = ApplicationCommandType.Message

	/** Call this to supply a command [body], to be called when the command is executed. **/
	public fun action(action: suspend C.(M?) -> Unit) {
		body = action
	}

	override fun validate() {
		super.validate()

		if (!::body.isInitialized) {
			throw InvalidCommandException(name, "No command body given.")
		}
	}

	/** Override this to implement your command's calling logic. Check subtypes for examples! **/
	public abstract override suspend fun call(
		event: MessageCommandInteractionCreateEvent,
		cache: MutableStringKeyedMap<Any>,
	)

	/** Override this to implement a way to respond to the user, regardless of whatever happens. **/
	public abstract suspend fun respondText(context: C, message: String, failureType: FailureReason<*>)

	/** If enabled, adds the initial Sentry breadcrumb to the given context. **/
	public open suspend fun firstSentryBreadcrumb(context: C) {
		if (sentry.enabled) {
			context.sentry.context(
				"command",

				mapOf(
					"name" to name,
					"type" to "message",
					"extension" to extension.name,
				)
			)

			context.sentry.breadcrumb(BreadcrumbType.User) {
				category = "command.application.message"
				message = "Message command \"$name\" called."

				data["command.name"] = name
			}
		}
	}

	override suspend fun runChecks(
		event: MessageCommandInteractionCreateEvent,
		cache: MutableStringKeyedMap<Any>,
	): Boolean {
		val locale = event.getLocale()
		val result = super.runChecks(event, cache)

		if (result) {
			settings.applicationCommandsBuilder.messageCommandChecks.forEach { check ->
				val context = CheckContextWithCache(event, locale, cache)

				check(context)

				if (!context.passed) {
					context.throwIfFailedWithMessage()

					return false
				}
			}

			extension.messageCommandChecks.forEach { check ->
				val context = CheckContextWithCache(event, locale, cache)

				check(context)

				if (!context.passed) {
					context.throwIfFailedWithMessage()

					return false
				}
			}
		}

		return result
	}

	/** A general way to handle errors thrown during the course of a command's execution. **/
	@Suppress("StringLiteralDuplication")
	public open suspend fun handleError(context: C, t: Throwable) {
		logger.error(t) { "Error during execution of $name message command (${context.event})" }

		if (sentry.enabled) {
			logger.trace { "Submitting error to sentry." }

			val sentryId = context.sentry.captureThrowable(t) {
				user = context.user.asUserOrNull()
				channel = context.channel.asChannelOrNull()
			}

			val errorMessage = if (sentryId != null) {
				logger.info { "Error submitted to Sentry: $sentryId" }

				if (extension.bot.extensions.containsKey(SENTRY_EXTENSION_NAME)) {
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
}
