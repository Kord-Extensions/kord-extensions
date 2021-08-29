@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.application.respond
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralUserResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(UserCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral user command. **/
public class EphemeralUserCommand(
    extension: Extension
) : UserCommand<EphemeralUserCommandContext>(extension) {
    /** Provide this tn open with a response, omit it to ack instead. **/
    public var initialResponseBuilder: InitialEphemeralUserResponseBuilder = null

    override suspend fun call(event: UserCommandInteractionCreateEvent) {
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

        val context = EphemeralUserCommandContext(event, this, response)

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

    override suspend fun respondText(context: EphemeralUserCommandContext, message: String) {
        context.respond { content = message }
    }
}
