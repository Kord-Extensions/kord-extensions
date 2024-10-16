/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.core.commands.application.slash

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.events.*
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale

public typealias InitialPublicSlashResponseBehavior =
	(suspend InteractionResponseCreateBuilder.(ChatInputCommandInteractionCreateEvent) -> Unit)?

/** Public slash command. **/
public class PublicSlashCommand<A : Arguments, M : ModalForm>(
	extension: Extension,

	public override val arguments: (() -> A)? = null,
	public override val modal: (() -> M)? = null,
	public override val parentCommand: SlashCommand<*, *, *>? = null,
	public override val parentGroup: SlashGroup? = null,
) : SlashCommand<PublicSlashCommandContext<A, M>, A, M>(extension) {
	/** @suppress Internal builder **/
	public var initialResponseBuilder: InitialPublicSlashResponseBehavior = null

	/** Call this to open with a response, omit it to ack instead. **/
	public fun initialResponse(body: InitialPublicSlashResponseBehavior) {
		initialResponseBuilder = body
	}

	override fun validate() {
		super.validate()

		if (modal != null && initialResponseBuilder != null) {
			throw InvalidCommandException(
				name,

				"You may not provide a modal builder and an initial response - pick one, not both."
			)
		}
	}

	override suspend fun call(event: ChatInputCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		findCommand(event).run(event, cache)
	}

	override suspend fun run(event: ChatInputCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(PublicSlashCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					PublicSlashCommandFailedChecksEvent(
						this,
						event,

						CoreTranslations.Checks.failedWithoutMessage
							.withLocale(event.getLocale())
					)
				)

				return
			}
		} catch (e: DiscordRelayedException) {
			event.interaction.respondEphemeral {
				settings.failureResponseBuilder(
					this,
					e.reason.withLocale(event.getLocale()),
					FailureReason.ProvidedCheckFailure(e)
				)
			}

			emitEventAsync(PublicSlashCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val modalObj = modal?.invoke()

		val response = if (initialResponseBuilder != null) {
			event.interaction.respondPublic { initialResponseBuilder!!(event) }
		} else if (modalObj != null) {
			componentRegistry.register(modalObj)

			val locale = event.getLocale()

			event.interaction.modal(
				modalObj.translateTitle(locale),
				modalObj.id
			) {
				modalObj.applyToBuilder(this, event.getLocale())
			}

			modalObj.awaitCompletion {
				componentRegistry.unregisterModal(modalObj)

				it?.deferPublicResponseUnsafe()
			} ?: return
		} else {
			event.interaction.deferPublicResponseUnsafe()
		}

		val context = PublicSlashCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context, this)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(
				context,
				e.reason.withLocale(context.getLocale()),
				FailureReason.OwnPermissionsCheckFailure(e)
			)

			emitEventAsync(PublicSlashCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		if (arguments != null) {
			try {
				val args = registry.argumentParser.parse(arguments, context)

				context.populateArgs(args)
			} catch (e: ArgumentParsingException) {
				respondText(
					context,
					e.reason.withLocale(context.getLocale()),
					FailureReason.ArgumentParsingFailure(e)
				)

				emitEventAsync(PublicSlashCommandFailedParsingEvent(this, event, e))

				return
			}
		}

		try {
			body(context, modalObj)
		} catch (t: Throwable) {
			emitEventAsync(PublicSlashCommandFailedWithExceptionEvent(this, event, t))

			if (t is DiscordRelayedException) {
				respondText(
					context,
					t.reason.withLocale(context.getLocale()),
					FailureReason.RelayedFailure(t)
				)

				return
			}

			handleError(context, t, this)

			return
		}

		emitEventAsync(PublicSlashCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
		context: PublicSlashCommandContext<A, M>,
		message: Key,
		failureType: FailureReason<*>,
	) {
		context.respond { settings.failureResponseBuilder(this, message, failureType) }
	}
}
