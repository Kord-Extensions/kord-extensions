@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.modules.unsafe.commands

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.application.user.UserCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.contexts.UnsafeUserCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialUserCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondPublic
import com.kotlindiscord.kord.extensions.types.FailureReason
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/** Like a standard user command, but with less safety features. **/
@UnsafeAPI
public class UnsafeUserCommand(
    extension: Extension
) : UserCommand<UnsafeUserCommandContext>(extension) {
    /** Initial response type. Change this to decide what happens when this user command action is executed. **/
    public var initialResponse: InitialUserCommandResponse = InitialUserCommandResponse.EphemeralAck

    override suspend fun call(event: UserCommandInteractionCreateEvent) {
        emitEventAsync(UnsafeUserCommandInvocationEvent(this, event))

        try {
            if (!runChecks(event)) {
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
            is InitialUserCommandResponse.EphemeralAck -> event.interaction.acknowledgeEphemeral()
            is InitialUserCommandResponse.PublicAck -> event.interaction.acknowledgePublic()

            is InitialUserCommandResponse.EphemeralResponse -> event.interaction.respondEphemeral {
                r.builder!!(event)
            }

            is InitialUserCommandResponse.PublicResponse -> event.interaction.respondPublic {
                r.builder!!(event)
            }

            is InitialUserCommandResponse.None -> null
        }

        val context = UnsafeUserCommandContext(event, this, response)

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
            body(context)
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
        context: UnsafeUserCommandContext,
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
