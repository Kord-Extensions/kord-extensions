@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralSelectMenuResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

/** Class representing an ephemeral-only select (dropdown) menu. **/
public open class EphemeralSelectMenu(timeoutTask: Task?) : SelectMenu<EphemeralSelectMenuContext>(timeoutTask) {
    /** @suppress Initial response builder. **/
    public open var initialResponseBuilder: InitialEphemeralSelectMenuResponseBuilder = null

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialEphemeralSelectMenuResponseBuilder) {
        initialResponseBuilder = body
    }

    override suspend fun call(event: SelectMenuInteractionCreateEvent): Unit = withLock {
        super.call(event)

        try {
            if (!runChecks(event)) {
                return@withLock
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral { settings.errorResponseBuilder(this, e.reason) }

            return@withLock
        }

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondEphemeral { initialResponseBuilder!!(event) }
        } else {
            if (!deferredAck) {
                event.interaction.acknowledgeEphemeralDeferredMessageUpdate()
            } else {
                event.interaction.acknowledgeEphemeral()
            }
        }

        val context = EphemeralSelectMenuContext(this, event, response)

        context.populate()

        firstSentryBreadcrumb(context, this)

        try {
            checkBotPerms(context)
            body(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason)
        } catch (t: Throwable) {
            handleError(context, t, this)
        }
    }

    override suspend fun respondText(context: EphemeralSelectMenuContext, message: String) {
        context.respond { settings.errorResponseBuilder(this, message) }
    }
}
