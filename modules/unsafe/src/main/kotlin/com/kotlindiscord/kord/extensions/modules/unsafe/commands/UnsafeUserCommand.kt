/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package com.kotlindiscord.kord.extensions.modules.unsafe.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.contexts.UnsafeUserCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialUserCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.ackEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondPublic
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

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
