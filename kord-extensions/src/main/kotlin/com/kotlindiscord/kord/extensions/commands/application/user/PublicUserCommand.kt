@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.application.respond
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.PublicInteractionResponseCreateBuilder

public typealias InitialPublicUserResponseBuilder =
    (suspend PublicInteractionResponseCreateBuilder.(UserCommandInteractionCreateEvent) -> Unit)?

/** Public user command. **/
public class PublicUserCommand(
    extension: Extension
) : UserCommand<PublicUserCommandContext>(extension) {
    /** @suppress Internal guilder **/
    public var initialResponseBuilder: InitialPublicUserResponseBuilder = null

    /** Call this tn open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialPublicUserResponseBuilder) {
        initialResponseBuilder = body
    }

    override suspend fun call(event: UserCommandInteractionCreateEvent) {
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

        val context = PublicUserCommandContext(event, this, response)

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

    override suspend fun respondText(context: PublicUserCommandContext, message: String) {
        context.respond { content = message }
    }
}
