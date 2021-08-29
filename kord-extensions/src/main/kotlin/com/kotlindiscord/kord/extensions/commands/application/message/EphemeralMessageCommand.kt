@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.application.respond
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(MessageCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral-followup-only message command. **/
public class EphemeralMessageCommand(
    extension: Extension
) : MessageCommand<EphemeralMessageCommandContext>(extension) {
    /** Provide this tn open with a response, omit it to ack instead. **/
    public var initialResponseBuilder: InitialEphemeralResponseBuilder = null

    override suspend fun call(event: MessageCommandInteractionCreateEvent) {
        try {
            if (!runChecks(event)) {
                return
            }
        } catch (e: CommandException) {
            event.interaction.respondEphemeral { content = e.reason }

            return
        }

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondEphemeral { initialResponseBuilder!!(event) }
        } else {
            event.interaction.acknowledgeEphemeral()
        }

        val context = EphemeralMessageCommandContext(event, this, response)

        context.populate()

        firstSentryBreadcrumb(context)

        try {
            checkBotPerms(context)
            body(context)
        } catch (e: CommandException) {
            respondText(context, e.reason)
        } catch (t: Throwable) {
            handleError(context, t)
        }
    }

    override suspend fun respondText(context: EphemeralMessageCommandContext, message: String) {
        context.respond { content = message }
    }
}
