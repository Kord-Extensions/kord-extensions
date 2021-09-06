package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.interactions.respond
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.PublicInteractionResponseCreateBuilder

public typealias InitialPublicButtonResponseBuilder =
    (suspend PublicInteractionResponseCreateBuilder.(ButtonInteractionCreateEvent) -> Unit)?

public open class PublicInteractionButton : InteractionButtonWithAction<PublicInteractionButtonContext>() {
    public open var style: ButtonStyle = ButtonStyle.Primary
    public open var initialResponseBuilder: InitialPublicButtonResponseBuilder = null

    override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji
            label = this@PublicInteractionButton.label
        }
    }

    override suspend fun call(event: ButtonInteractionCreateEvent) {
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

        val context = PublicInteractionButtonContext(this, event, response)

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

    override suspend fun respondText(context: PublicInteractionButtonContext, message: String) {
        context.respond { content = message }
    }
}
