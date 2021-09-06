package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.EphemeralInteractionResponseCreateBuilder

public typealias InitialEphemeralButtonResponseBuilder =
    (suspend EphemeralInteractionResponseCreateBuilder.(ButtonInteractionCreateEvent) -> Unit)?

public open class EphemeralInteractionButton : InteractionButtonWithAction<EphemeralInteractionButtonContext>() {
    public open var style: ButtonStyle = ButtonStyle.Primary
    public open var initialResponseBuilder: InitialEphemeralButtonResponseBuilder = null

    override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji
            label = this@EphemeralInteractionButton.label
        }
    }

    override suspend fun call(event: ButtonInteractionCreateEvent) {
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
            if (!deferredAck) {
                event.interaction.acknowledgeEphemeralDeferredMessageUpdate()
            } else {
                event.interaction.acknowledgeEphemeral()
            }
        }

        val context = EphemeralInteractionButtonContext(this, event, response)

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

    override fun validate() {
        super.validate()

        if (style == ButtonStyle.Link) {
            error("The Link button style is reserved for link buttons.")
        }
    }

    override suspend fun respondText(context: EphemeralInteractionButtonContext, message: String) {
        context.respond { content = message }
    }
}
