@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.modules.unsafe.commands

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.application.message.MessageCommand
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.contexts.UnsafeMessageCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.types.InitialMessageCommandResponse
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondEphemeral
import com.kotlindiscord.kord.extensions.modules.unsafe.types.respondPublic
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
        } catch (e: CommandException) {
            event.interaction.respondPublic { content = e.reason }

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
        }

        val context = UnsafeMessageCommandContext(event, this, response)

        context.populate()

        firstSentryBreadcrumb(context)

        try {
            checkBotPerms(context)
        } catch (e: CommandException) {
            respondText(context, e.reason)
            emitEventAsync(UnsafeMessageCommandFailedChecksEvent(this, event, e.reason))

            return
        }

        try {
            checkBotPerms(context)
            body(context)
        } catch (t: Throwable) {
            if (t is CommandException) {
                respondText(context, t.reason)
            }

            emitEventAsync(UnsafeMessageCommandFailedWithExceptionEvent(this, event, t))
            handleError(context, t)

            return
        }

        emitEventAsync(UnsafeMessageCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(context: UnsafeMessageCommandContext, message: String) {
        when (context.interactionResponse) {
            is PublicInteractionResponseBehavior -> context.respondPublic { content = message }
            is EphemeralInteractionResponseBehavior -> context.respondEphemeral { content = message }
        }
    }
}
