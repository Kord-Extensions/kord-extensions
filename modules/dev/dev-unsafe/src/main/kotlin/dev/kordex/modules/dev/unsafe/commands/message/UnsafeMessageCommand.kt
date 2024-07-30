/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.commands.message

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.application.message.MessageCommand
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.commands.UnsafeMessageCommandFailedChecksEvent
import dev.kordex.modules.dev.unsafe.commands.UnsafeMessageCommandFailedWithExceptionEvent
import dev.kordex.modules.dev.unsafe.commands.UnsafeMessageCommandInvocationEvent
import dev.kordex.modules.dev.unsafe.commands.UnsafeMessageCommandSucceededEvent
import dev.kordex.modules.dev.unsafe.contexts.UnsafeCommandMessageCommandContext

/** Like a standard message command, but with less safety features. **/
@UnsafeAPI
public class UnsafeMessageCommand<M : ModalForm>(
	extension: Extension,
	public override val modal: (() -> M)? = null,
) : MessageCommand<UnsafeCommandMessageCommandContext<M>, M>(extension) {
	/** Initial response type. Change this to decide what happens when this message command action is executed. **/
	public var initialResponse: InitialMessageCommandResponse = InitialMessageCommandResponse.EphemeralAck

	override suspend fun call(event: MessageCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(UnsafeMessageCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					UnsafeMessageCommandFailedChecksEvent(
						this,
						event,
						"Checks failed without a message."
					)
				)
				return
			}
		} catch (e: DiscordRelayedException) {
			event.interaction.respondEphemeral {
				settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
			}

			emitEventAsync(UnsafeMessageCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val response = when (val r = initialResponse) {
			is InitialMessageCommandResponse.EphemeralAck -> event.interaction.deferEphemeralResponseUnsafe()
			is InitialMessageCommandResponse.PublicAck -> event.interaction.deferPublicResponseUnsafe()

			is InitialMessageCommandResponse.EphemeralResponse -> event.interaction.respondEphemeral {
				r.builder!!(event)
			}

			is InitialMessageCommandResponse.PublicResponse -> event.interaction.respondPublic {
				r.builder!!(event)
			}

			is InitialMessageCommandResponse.None -> null
		}

		val context = UnsafeCommandMessageCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context)

		try {
			checkBotPerms(context)
		} catch (t: DiscordRelayedException) {
			emitEventAsync(UnsafeMessageCommandFailedChecksEvent(this, event, t.reason))
			respondText(context, t.reason, FailureReason.OwnPermissionsCheckFailure(t))

			return
		}

		try {
			body(context, null)
		} catch (t: Throwable) {
			if (t is DiscordRelayedException) {
				respondText(context, t.reason, FailureReason.RelayedFailure(t))
			}

			emitEventAsync(UnsafeMessageCommandFailedWithExceptionEvent(this, event, t))
			handleError(context, t)

			return
		}

		emitEventAsync(UnsafeMessageCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
        context: UnsafeCommandMessageCommandContext<M>,
        message: String,
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
