/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")
@file:OptIn(KordUnsafe::class)

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.InvalidCommandException
import com.kotlindiscord.kord.extensions.commands.events.EphemeralMessageCommandFailedChecksEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralMessageCommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralMessageCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralMessageCommandSucceededEvent
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

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
        val invocationEvent = EphemeralMessageCommandInvocationEvent(this, event)
        emitEventAsync(invocationEvent)

        try {
            // cooldown and rate-limits
            if (useLimited(invocationEvent)) return

            // checks
            if (!runChecks(event, cache)) {
                emitEventAsync(
                    EphemeralMessageCommandFailedChecksEvent(
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

        val context = EphemeralMessageCommandContext(event, this, response, cache)

        context.populate()

        firstSentryBreadcrumb(context)

        try {
            checkBotPerms(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))
            emitEventAsync(EphemeralMessageCommandFailedChecksEvent(this, event, e.reason))

            return
        }

        try {
            body(context, modalObj)
        } catch (t: Throwable) {
            emitEventAsync(EphemeralMessageCommandFailedWithExceptionEvent(this, event, t))
            onSuccessUseLimitUpdate(context, invocationEvent, false)

            if (t is DiscordRelayedException) {
                respondText(context, t.reason, FailureReason.RelayedFailure(t))

                return
            }

            handleError(context, t)

            return
        }
        onSuccessUseLimitUpdate(context, invocationEvent, true)

        emitEventAsync(EphemeralMessageCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(
        context: EphemeralMessageCommandContext<M>,
        message: String,
        failureType: FailureReason<*>,
    ) {
        context.respond { settings.failureResponseBuilder(this, message, failureType) }
    }
}
