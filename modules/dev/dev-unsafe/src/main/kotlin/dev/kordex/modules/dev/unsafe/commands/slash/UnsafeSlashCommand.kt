/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.commands.slash

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.SlashCommand
import dev.kordex.core.commands.application.slash.SlashGroup
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.*
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.contexts.UnsafeCommandSlashCommandContext

/** Like a standard slash command, but with less safety features. **/
@UnsafeAPI
public class UnsafeSlashCommand<A : Arguments, M : UnsafeModalForm>(
	extension: Extension,

	public override val arguments: (() -> A)? = null,
	public override val modal: (() -> M)? = null,
	public override val parentCommand: SlashCommand<*, *, *>? = null,
	public override val parentGroup: SlashGroup? = null,
) : SlashCommand<UnsafeCommandSlashCommandContext<A, M>, A, M>(extension) {
	/** Initial response type. Change this to decide what happens when this slash command is executed. **/
	public var initialResponse: InitialSlashCommandResponse = InitialSlashCommandResponse.EphemeralAck

	override fun validate() {
		super.validate()

		if (modal != null && initialResponse != InitialSlashCommandResponse.None) {
			throw InvalidCommandException(
				name,

				"You may not provide a modal builder and an initial response - pick one, not both."
			)
		}
	}

	override suspend fun call(event: ChatInputCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		findCommand(event).run(event, cache)
	}

	@OptIn(InternalAPI::class)
	override suspend fun run(event: ChatInputCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(UnsafeSlashCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					UnsafeSlashCommandFailedChecksEvent(
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
				settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
			}

			emitEventAsync(UnsafeSlashCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val modalObj = modal?.invoke()

		val response = when (val r = initialResponse) {
			is InitialSlashCommandResponse.EphemeralAck -> event.interaction.deferEphemeralResponseUnsafe()
			is InitialSlashCommandResponse.PublicAck -> event.interaction.deferPublicResponseUnsafe()

			is InitialSlashCommandResponse.EphemeralResponse -> event.interaction.respondEphemeral {
				r.builder!!(event)
			}

			is InitialSlashCommandResponse.PublicResponse -> event.interaction.respondPublic {
				r.builder!!(event)
			}

			is InitialSlashCommandResponse.None -> if (modalObj != null) {
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

					modalObj.respond(it)
				} ?: return
			} else {
				null
			}
		}

		val context = UnsafeCommandSlashCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context, this)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))
			emitEventAsync(UnsafeSlashCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		try {
			if (arguments != null) {
				val args = registry.argumentParser.parse(arguments, context)

				context.populateArgs(args)
			}
		} catch (e: ArgumentParsingException) {
			respondText(context, e.reason, FailureReason.ArgumentParsingFailure(e))
			emitEventAsync(UnsafeSlashCommandFailedParsingEvent(this, event, e))

			return
		}

		try {
			body(context, null)
		} catch (t: Throwable) {
			if (t is DiscordRelayedException) {
				respondText(context, t.reason, FailureReason.RelayedFailure(t))
			}

			emitEventAsync(UnsafeSlashCommandFailedWithExceptionEvent(this, event, t))
			handleError(context, t, this)

			return
		}

		emitEventAsync(UnsafeSlashCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
        context: UnsafeCommandSlashCommandContext<A, M>,
        message: Key,
        failureType: FailureReason<*>,
	) {
		when (context.interactionResponse) {
			is PublicMessageInteractionResponseBehavior -> context.respondPublic {
				settings.failureResponseBuilder(this, message, failureType)
			}

			is EphemeralMessageInteractionResponseBehavior -> context.respondEphemeral {
				settings.failureResponseBuilder(this, message, failureType)
			}

			null -> context.ackEphemeral {
				settings.failureResponseBuilder(this, message, failureType)
			}
		}
	}
}
