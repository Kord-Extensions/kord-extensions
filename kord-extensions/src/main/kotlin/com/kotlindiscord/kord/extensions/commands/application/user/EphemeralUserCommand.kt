/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandFailedChecksEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandSucceededEvent
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialEphemeralUserResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(UserCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral user command. **/
public class EphemeralUserCommand<M : ModalForm>(
	extension: Extension,
	public override val modal: (() -> M)? = null,
) : UserCommand<EphemeralUserCommandContext<M>, M>(extension) {
	/** @suppress Internal guilder **/
	public var initialResponseBuilder: InitialEphemeralUserResponseBuilder = null

	/** Call this to open with a response, omit it to ack instead. **/
	public fun initialResponse(body: InitialEphemeralUserResponseBuilder) {
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

	override suspend fun call(event: UserCommandInteractionCreateEvent, cache: MutableStringKeyedMap<Any>) {
		emitEventAsync(EphemeralUserCommandInvocationEvent(this, event))

		try {
			if (!runChecks(event, cache)) {
				emitEventAsync(
					EphemeralUserCommandFailedChecksEvent(
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

			emitEventAsync(EphemeralUserCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		val modalObj = modal?.invoke()

		val response = if (initialResponseBuilder != null) {
			event.interaction.respondEphemeral { initialResponseBuilder!!(event) }
		} else if (modalObj != null) {
			componentRegistry.register(modalObj)

			val locale = event.getLocale()

			event.interaction.modal(
				modalObj.translateTitle(locale, resolvedBundle),
				modalObj.id
			) {
				modalObj.applyToBuilder(this, event.getLocale(), resolvedBundle)
			}

			modalObj.awaitCompletion {
				componentRegistry.unregisterModal(modalObj)

				it?.deferEphemeralResponseUnsafe()
			} ?: return
		} else {
			event.interaction.deferEphemeralResponseUnsafe()
		}

		val context = EphemeralUserCommandContext(event, this, response, cache)

		context.populate()

		firstSentryBreadcrumb(context)

		try {
			checkBotPerms(context)
		} catch (e: DiscordRelayedException) {
			respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))
			emitEventAsync(EphemeralUserCommandFailedChecksEvent(this, event, e.reason))

			return
		}

		try {
			body(context, modalObj)
		} catch (t: Throwable) {
			emitEventAsync(EphemeralUserCommandFailedWithExceptionEvent(this, event, t))

			if (t is DiscordRelayedException) {
				respondText(context, t.reason, FailureReason.RelayedFailure(t))

				return
			}

			handleError(context, t)
		}

		emitEventAsync(EphemeralUserCommandSucceededEvent(this, event))
	}

	override suspend fun respondText(
		context: EphemeralUserCommandContext<M>,
		message: String,
		failureType: FailureReason<*>,
	) {
		context.respond { settings.failureResponseBuilder(this, message, failureType) }
	}
}
