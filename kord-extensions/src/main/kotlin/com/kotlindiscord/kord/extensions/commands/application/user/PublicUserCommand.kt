@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.events.PublicUserCommandFailedChecksEvent
import com.kotlindiscord.kord.extensions.commands.events.PublicUserCommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.commands.events.PublicUserCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.PublicUserCommandSucceededEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.respondEphemeral
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

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialPublicUserResponseBuilder) {
        initialResponseBuilder = body
    }

    override suspend fun call(event: UserCommandInteractionCreateEvent) {
        emitEventAsync(PublicUserCommandInvocationEvent(this, event))

        try {
            if (!runChecks(event)) {
                emitEventAsync(
                    PublicUserCommandFailedChecksEvent(
                        this,
                        event,
                        "Checks failed without a message."
                    )
                )

                return
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral { content = e.reason }

            emitEventAsync(PublicUserCommandFailedChecksEvent(this, event, e.reason))

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
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason)
            emitEventAsync(PublicUserCommandFailedChecksEvent(this, event, e.reason))

            return
        }

        try {
            body(context)
        } catch (t: Throwable) {
            if (t is DiscordRelayedException) {
                respondText(context, t.reason)
            }

            emitEventAsync(PublicUserCommandFailedWithExceptionEvent(this, event, t))
            handleError(context, t)
        }

        emitEventAsync(PublicUserCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(context: PublicUserCommandContext, message: String) {
        context.respond { content = message }
    }
}
