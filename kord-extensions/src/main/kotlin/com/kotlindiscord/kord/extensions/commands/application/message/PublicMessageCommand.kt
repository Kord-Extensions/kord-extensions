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
import com.kotlindiscord.kord.extensions.commands.events.PublicMessageCommandFailedChecksEvent
import com.kotlindiscord.kord.extensions.commands.events.PublicMessageCommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.commands.events.PublicMessageCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.PublicMessageCommandSucceededEvent
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

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
                        "Checks failed without a message."
                    )
                )

                return
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral {
                settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
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
                modalObj.translateTitle(locale, resolvedBundle),
                modalObj.id
            ) {
                modalObj.applyToBuilder(this, event.getLocale(), resolvedBundle)
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
            respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))
            emitEventAsync(PublicMessageCommandFailedChecksEvent(this, event, e.reason))

            return
        }

        try {
            body(context, modalObj)
        } catch (t: Throwable) {
            emitEventAsync(PublicMessageCommandFailedWithExceptionEvent(this, event, t))

            if (t is DiscordRelayedException) {
                respondText(context, t.reason, FailureReason.RelayedFailure(t))

                return
            }

            handleError(context, t)

            return
        }

        emitEventAsync(PublicMessageCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(
        context: PublicMessageCommandContext<M>,
        message: String,
        failureType: FailureReason<*>
    ) {
        context.respond { settings.failureResponseBuilder(this, message, failureType) }
    }
}
