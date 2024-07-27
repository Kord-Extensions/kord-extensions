/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package dev.kordex.modules.dev.unsafe.commands

import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.application.user.UserCommand
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.extensions.Extension
import dev.kordex.core.types.FailureReason
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.contexts.UnsafeUserCommandContext
import dev.kordex.modules.dev.unsafe.types.InitialUserCommandResponse
import dev.kordex.modules.dev.unsafe.types.ackEphemeral
import dev.kordex.modules.dev.unsafe.types.respondEphemeral
import dev.kordex.modules.dev.unsafe.types.respondPublic

/** Like a standard user command, but with less safety features. **/
@UnsafeAPI
public class UnsafeUserCommand<M : ModalForm>(
	extension: Extension,
	public override val modal: (() -> M)? = null,
) : UserCommand<UnsafeUserCommandContext<M>, M>(extension) {
	/** Initial response type. Change this to decide what happens when this user command action is executed. **/
	public var initialResponse: InitialUserCommandResponse = InitialUserCommandResponse.EphemeralAck

	override suspend fun call(event: UserCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(UnsafeUserCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					UnsafeUserCommandFailedChecksEvent(
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

			emitEventAsync(UnsafeUserCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val response = when (val r = initialResponse) {
			is InitialUserCommandResponse.EphemeralAck -> event.interaction.deferEphemeralResponseUnsafe()
			is InitialUserCommandResponse.PublicAck -> event.interaction.deferPublicResponseUnsafe()

			is InitialUserCommandResponse.EphemeralResponse -> event.interaction.respondEphemeral {
				r.builder!!(event)
			}

			is InitialUserCommandResponse.PublicResponse -> event.interaction.respondPublic {
				r.builder!!(event)
			}

			is InitialUserCommandResponse.None -> null
		}

		val context = UnsafeUserCommandContext(event, this, response, cache)

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
		context: UnsafeUserCommandContext<M>,
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
