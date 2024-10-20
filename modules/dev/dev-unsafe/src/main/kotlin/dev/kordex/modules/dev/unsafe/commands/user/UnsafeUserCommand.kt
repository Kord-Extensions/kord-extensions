/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.commands.user

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.InvalidCommandException
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.commands.application.user.UserCommand
import dev.kordex.core.extensions.Extension
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.getLocale
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeUserCommandFailedChecksEvent
import dev.kordex.modules.dev.unsafe.commands.UnsafeUserCommandFailedWithExceptionEvent
import dev.kordex.modules.dev.unsafe.commands.UnsafeUserCommandInvocationEvent
import dev.kordex.modules.dev.unsafe.commands.UnsafeUserCommandSucceededEvent
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.contexts.UnsafeCommandUserCommandContext

/** Like a standard user command, but with less safety features. **/
@UnsafeAPI
public class UnsafeUserCommand<M : UnsafeModalForm>(
	extension: Extension,
	public override val modal: (() -> M)? = null,
) : UserCommand<UnsafeCommandUserCommandContext<M>, M>(extension) {
	/** Initial response type. Change this to decide what happens when this user command action is executed. **/
	public var initialResponse: InitialUserCommandResponse = InitialUserCommandResponse.EphemeralAck

	override fun validate() {
		super.validate()

		if (modal != null && initialResponse != InitialUserCommandResponse.None) {
			throw InvalidCommandException(
				name,

				"You may not provide a modal builder and an initial response - pick one, not both."
			)
		}
	}

	@OptIn(InternalAPI::class)
	override suspend fun call(event: UserCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(UnsafeUserCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					UnsafeUserCommandFailedChecksEvent(
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

			emitEventAsync(UnsafeUserCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val modalObj = modal?.invoke()

		val response = when (val r = initialResponse) {
			is InitialUserCommandResponse.EphemeralAck -> event.interaction.deferEphemeralResponseUnsafe()
			is InitialUserCommandResponse.PublicAck -> event.interaction.deferPublicResponseUnsafe()

			is InitialUserCommandResponse.EphemeralResponse -> event.interaction.respondEphemeral {
				r.builder!!(event)
			}

			is InitialUserCommandResponse.PublicResponse -> event.interaction.respondPublic {
				r.builder!!(event)
			}

			is InitialUserCommandResponse.None -> if (modalObj != null) {
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

		val context = UnsafeCommandUserCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context)

		try {
			checkBotPerms(context)
		} catch (t: DiscordRelayedException) {
			emitEventAsync(UnsafeUserCommandFailedChecksEvent(this, event, t.reason))
			respondText(context, t.reason, FailureReason.OwnPermissionsCheckFailure(t))

			return
		}

		try {
			body(context, null)
		} catch (t: Throwable) {
			if (t is DiscordRelayedException) {
				respondText(context, t.reason, FailureReason.RelayedFailure(t))
			}

			emitEventAsync(UnsafeUserCommandFailedWithExceptionEvent(this, event, t))
			handleError(context, t)

			return
		}

		emitEventAsync(UnsafeUserCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
        context: UnsafeCommandUserCommandContext<M>,
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
