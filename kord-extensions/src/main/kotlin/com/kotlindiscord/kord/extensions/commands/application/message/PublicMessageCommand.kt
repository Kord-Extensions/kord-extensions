@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.message

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.application.respond
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.MessageCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.PublicInteractionResponseCreateBuilder

public typealias InitialPublicResponseBuilder =
    (suspend PublicInteractionResponseCreateBuilder.(MessageCommandInteractionCreateEvent) -> Unit)?

/** Public-followup-only message command. **/
public class PublicMessageCommand(
    extension: Extension
) : MessageCommand<PublicMessageCommandContext>(extension) {
    /** Provide this tn open with a response, omit it to ack instead. **/
    public var initialResponseBuilder: InitialPublicResponseBuilder = null

    override suspend fun call(event: MessageCommandInteractionCreateEvent) {
        try {
            if (!runChecks(event)) {
                return
            }
        } catch (e: CommandException) {
            event.interaction.respondPublic { content = e.reason }

            return
        }

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondPublic { initialResponseBuilder!!(event) }
        } else {
            event.interaction.acknowledgePublic()
        }

        val context = PublicMessageCommandContext(event, this, response)

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

    override suspend fun respondText(context: PublicMessageCommandContext, message: String) {
        context.respond { content = message }
    }
}
