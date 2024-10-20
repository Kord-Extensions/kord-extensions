/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.core.commands.application.message

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.commands.events.EphemeralMessageCommandFailedChecksEvent
import dev.kordex.core.commands.events.EphemeralMessageCommandFailedWithExceptionEvent
import dev.kordex.core.commands.events.EphemeralMessageCommandInvocationEvent
import dev.kordex.core.commands.events.EphemeralMessageCommandSucceededEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale

public typealias InitialEphemeralMessageResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(MessageCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral message command. **/
public class EphemeralMessageCommand<M : ModalForm>(
	extension: Extension,
	public override val modal: (() -> M)? = null,
) : MessageCommand<EphemeralMessageCommandContext<M>, M>(extension) {
	/** @suppress Internal guilder **/
	public var initialResponseBuilder: InitialEphemeralMessageResponseBuilder = null

	/** Call this to open with a response, omit it to ack instead. **/
	public fun initialResponse(body: InitialEphemeralMessageResponseBuilder) {
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

	override suspend fun call(event: MessageCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(EphemeralMessageCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					EphemeralMessageCommandFailedChecksEvent(
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

			emitEventAsync(EphemeralMessageCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val modalObj = modal?.invoke()

		val response = if (initialResponseBuilder != null) {
			event.interaction.respondEphemeral { initialResponseBuilder!!(event) }
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

				it?.deferEphemeralResponseUnsafe()
			} ?: return
		} else {
			event.interaction.deferEphemeralResponseUnsafe()
		}

		val context = EphemeralMessageCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(
				context,
				e.reason.withLocale(context.getLocale()),
				FailureReason.OwnPermissionsCheckFailure(e)
			)

			emitEventAsync(EphemeralMessageCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		try {
			body(context, modalObj)
		} catch (t: Throwable) {
			emitEventAsync(EphemeralMessageCommandFailedWithExceptionEvent(this, event, t))

			if (t is DiscordRelayedException) {
				respondText(
					context,
					t.reason.withLocale(context.getLocale()),
					FailureReason.RelayedFailure(t)
				)

				return
			}

			handleError(context, t)

			return
		}

		emitEventAsync(EphemeralMessageCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
		context: EphemeralMessageCommandContext<M>,
		message: Key,
		failureType: FailureReason<*>,
	) {
		context.respond { settings.failureResponseBuilder(this, message, failureType) }
	}
}
