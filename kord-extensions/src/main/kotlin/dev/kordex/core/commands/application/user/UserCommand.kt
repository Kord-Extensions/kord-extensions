/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.application.user

import dev.kord.common.entity.ApplicationCommandType
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.checks.types.CheckContextWithCache
import dev.kordex.core.commands.application.ApplicationCommand
import dev.kordex.core.components.ComponentRegistry
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.impl.SENTRY_EXTENSION_NAME
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.sentry.BreadcrumbType
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

/**
 * User context command, for right-click actions on users.
 * @param modal Callable returning a `ModalForm` object, if any
 */
public abstract class UserCommand<C : UserCommandContext<C, M>, M : ModalForm>(
	extension: Extension,
	public open val modal: (() -> M)? = null,
) : ApplicationCommand<UserCommandInteractionCreateEvent>(extension) {
	private val logger: KLogger = KotlinLogging.logger {}

	/** @suppress This is only meant for use by code that extends the command system. **/
	public val componentRegistry: ComponentRegistry by inject()

	/** Command body, to be called when the command is executed. **/
	public lateinit var body: suspend C.(M?) -> Unit

	override val type: ApplicationCommandType = ApplicationCommandType.User

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
		event: UserCommandInteractionCreateEvent,
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
					"type" to "user",
					"extension" to extension.name,
				)
			)

			context.sentry.breadcrumb(BreadcrumbType.User) {
				category = "command.application.user"
				message = "User command \"$name\" called."

				channel = context.channel.asChannelOrNull()
				guild = context.guild?.asGuildOrNull()

				data["command"] = name
			}
		}
	}

	override suspend fun runChecks(
		event: UserCommandInteractionCreateEvent,
		cache: MutableStringKeyedMap<Any>,
	): Boolean {
		val locale = event.getLocale()
		val result = super.runChecks(event, cache)

		if (result) {
			settings.applicationCommandsBuilder.userCommandChecks.forEach { check ->
				val context = CheckContextWithCache(event, locale, cache)

				check(context)

				if (!context.passed) {
					context.throwIfFailedWithMessage()

					return false
				}
			}

			extension.userCommandChecks.forEach { check ->
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
		logger.error(t) { "Error during execution of $name user command (${context.event})" }

		if (sentry.enabled) {
			logger.trace { "Submitting error to sentry." }

			val sentryId = context.sentry.captureThrowable(t) {
				user = context.user.asUserOrNull()
				channel = context.channel.asChannelOrNull()
			}

			val errorMessage = if (sentryId != null) {
				logger.info { "Error submitted to Sentry: $sentryId" }

				if (extension.bot.extensions.containsKey(SENTRY_EXTENSION_NAME)) {
					CoreTranslations.Commands.Error.User.Sentry.slash
						.withLocale(context.getLocale())
						.translate(sentryId)
				} else {
					CoreTranslations.Commands.Error.user
						.withLocale(context.getLocale())
						.translate()
				}
			} else {
				CoreTranslations.Commands.Error.user
					.withLocale(context.getLocale())
					.translate()
			}

			respondText(context, errorMessage, FailureReason.ExecutionError(t))
		} else {
			respondText(
				context,

				CoreTranslations.Commands.Error.user
					.withLocale(context.getLocale())
					.translate(),

				FailureReason.ExecutionError(t)
			)
		}
	}
}
