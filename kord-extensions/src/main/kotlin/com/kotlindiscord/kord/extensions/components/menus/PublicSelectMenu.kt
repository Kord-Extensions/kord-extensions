package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.message.create.PublicInteractionResponseCreateBuilder

public typealias InitialPublicSelectMenuResponseBuilder =
    (suspend PublicInteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

public open class PublicSelectMenu : SelectMenu<PublicSelectMenuContext>() {
    public open var initialResponseBuilder: InitialPublicSelectMenuResponseBuilder = null

    override suspend fun call(event: SelectMenuInteractionCreateEvent) {
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
        } catch (e: CommandException) {
            respondText(context, e.reason)
        } catch (t: Throwable) {
            handleError(context, t, this)
        }
    }

    override suspend fun respondText(context: PublicSelectMenuContext, message: String) {
        context.respond { content = message }
    }
}
