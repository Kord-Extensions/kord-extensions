@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.modules.unsafe.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.contexts.UnsafeMessageCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialMessageCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondPublic
import com.kotlindiscord.kord.extensions.types.FailureReason
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent

/** Like a standard message command, but with less safety features. **/
@UnsafeAPI
public class UnsafeMessageCommand(
    extension: Extension
) : MessageCommand<UnsafeMessageCommandContext>(extension) {
    /** Initial response type. Change this to decide what happens when this message command action is executed. **/
    public var initialResponse: InitialMessageCommandResponse = InitialMessageCommandResponse.EphemeralAck

    override suspend fun call(event: MessageCommandInteractionCreateEvent) {
        emitEventAsync(UnsafeMessageCommandInvocationEvent(this, event))

        try {
            if (!runChecks(event)) {
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
            is InitialMessageCommandResponse.EphemeralAck -> event.interaction.acknowledgeEphemeral()
            is InitialMessageCommandResponse.PublicAck -> event.interaction.acknowledgePublic()

            is InitialMessageCommandResponse.EphemeralResponse -> event.interaction.respondEphemeral {
                r.builder!!(event)
            }

            is InitialMessageCommandResponse.PublicResponse -> event.interaction.respondPublic {
                r.builder!!(event)
            }

            is InitialMessageCommandResponse.None -> null
        }

        val context = UnsafeMessageCommandContext(event, this, response)

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
            body(context)
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
        context: UnsafeMessageCommandContext,
        message: String,
        failureType: FailureReason<*>
    ) {
        when (context.interactionResponse) {
            is PublicInteractionResponseBehavior -> context.respondPublic {
                settings.failureResponseBuilder(this, message, failureType)
            }

            is EphemeralInteractionResponseBehavior -> context.respondEphemeral {
                settings.failureResponseBuilder(this, message, failureType)
            }
        }
    }
}
