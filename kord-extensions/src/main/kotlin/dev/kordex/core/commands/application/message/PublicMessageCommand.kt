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
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.commands.events.PublicMessageCommandFailedChecksEvent
import dev.kordex.core.commands.events.PublicMessageCommandFailedWithExceptionEvent
import dev.kordex.core.commands.events.PublicMessageCommandInvocationEvent
import dev.kordex.core.commands.events.PublicMessageCommandSucceededEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale

public typealias InitialPublicMessageResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(MessageCommandInteractionCreateEvent) -> Unit)?

/** Public message command. **/
public class PublicMessageCommand<M : ModalForm>(
	extension: Extension,
	public override val modal: (() -> M)? = null,
) : MessageCommand<PublicMessageCommandContext<M>, M>(extension) {
	/** @suppress Internal guilder **/
	public var initialResponseBuilder: InitialPublicMessageResponseBuilder = null

	/** Call this to open with a response, omit it to ack instead. **/
	public fun initialResponse(body: InitialPublicMessageResponseBuilder) {
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
		emitEventAsync(PublicMessageCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					PublicMessageCommandFailedChecksEvent(
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

			emitEventAsync(PublicMessageCommandFailedChecksEvent(this, event, e.reason))

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

		val context = PublicMessageCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(
				context,
				e.reason.withContext(context),
				FailureReason.OwnPermissionsCheckFailure(e)
			)

			emitEventAsync(PublicMessageCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		try {
			body(context, modalObj)
		} catch (t: Throwable) {
			emitEventAsync(PublicMessageCommandFailedWithExceptionEvent(this, event, t))

			if (t is DiscordRelayedException) {
				respondText(
					context,
					t.reason.withContext(context),
					FailureReason.RelayedFailure(t)
				)

				return
			}

			handleError(context, t)

			return
		}

		emitEventAsync(PublicMessageCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
		context: PublicMessageCommandContext<M>,
		message: Key,
		failureType: FailureReason<*>,
	) {
		context.respond { settings.failureResponseBuilder(this, message, failureType) }
	}
}
