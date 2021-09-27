@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.create.PublicInteractionResponseCreateBuilder

public typealias InitialPublicSelectMenuResponseBuilder =
    (suspend PublicInteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

/** Class representing a public-only select (dropdown) menu. **/
public open class PublicSelectMenu(timeoutTask: Task?) : SelectMenu<PublicSelectMenuContext>(timeoutTask) {
    /** @suppress Initial response builder. **/
    public open var initialResponseBuilder: InitialPublicSelectMenuResponseBuilder = null

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialPublicSelectMenuResponseBuilder) {
        initialResponseBuilder = body
    }

    override suspend fun call(event: SelectMenuInteractionCreateEvent): Unit = withLock {
        super.call(event)

        try {
            if (!runChecks(event)) {
                return@withLock
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral { content = e.reason }

            return@withLock
        }

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondPublic { initialResponseBuilder!!(event) }
        } else {
            if (!deferredAck) {
                event.interaction.acknowledgePublic()
            } else {
                event.interaction.acknowledgePublicDeferredMessageUpdate()
            }
        }

        val context = PublicSelectMenuContext(this, event, response)

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

    override suspend fun respondText(context: PublicSelectMenuContext, message: String) {
        context.respond { content = message }
    }
}
