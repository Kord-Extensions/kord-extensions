@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandFailedChecksEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandFailedWithExceptionEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.EphemeralUserCommandSucceededEvent
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.types.respond
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralUserResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(UserCommandInteractionCreateEvent) -> Unit)?

/** Ephemeral user command. **/
public class EphemeralUserCommand(
    extension: Extension
) : UserCommand<EphemeralUserCommandContext>(extension) {
    /** @suppress Internal guilder **/
    public var initialResponseBuilder: InitialEphemeralUserResponseBuilder = null

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialEphemeralUserResponseBuilder) {
        initialResponseBuilder = body
    }

    override suspend fun call(event: UserCommandInteractionCreateEvent) {
        emitEventAsync(EphemeralUserCommandInvocationEvent(this, event))

        try {
            if (!runChecks(event)) {
                emitEventAsync(
                    EphemeralUserCommandFailedChecksEvent(
                        this,
                        event,
                        "Checks failed without a message."
                    )
                )

                return
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral { settings.errorResponseBuilder(this, e.reason) }

            emitEventAsync(EphemeralUserCommandFailedChecksEvent(this, event, e.reason))

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
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason)
            emitEventAsync(EphemeralUserCommandFailedChecksEvent(this, event, e.reason))

            return
        }

        try {
            body(context)
        } catch (t: Throwable) {
            if (t is DiscordRelayedException) {
                respondText(context, t.reason)
            }

            emitEventAsync(EphemeralUserCommandFailedWithExceptionEvent(this, event, t))
            handleError(context, t)
        }

        emitEventAsync(EphemeralUserCommandSucceededEvent(this, event))
    }

    override suspend fun respondText(context: EphemeralUserCommandContext, message: String) {
        context.respond { settings.errorResponseBuilder(this, message) }
    }
}
