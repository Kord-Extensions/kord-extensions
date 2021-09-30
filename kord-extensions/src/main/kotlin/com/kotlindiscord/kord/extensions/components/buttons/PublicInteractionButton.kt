@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.PublicInteractionResponseCreateBuilder

public typealias InitialPublicButtonResponseBuilder =
    (suspend PublicInteractionResponseCreateBuilder.(ButtonInteractionCreateEvent) -> Unit)?

/** Class representing a public-only button component. **/
public open class PublicInteractionButton(
    timeoutTask: Task?
) : InteractionButtonWithAction<PublicInteractionButtonContext>(timeoutTask) {
    /** Button style - anything but Link is valid. **/
    public open var style: ButtonStyle = ButtonStyle.Primary

    /** @suppress Initial response builder. **/
    public open var initialResponseBuilder: InitialPublicButtonResponseBuilder = null

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialPublicButtonResponseBuilder) {
        initialResponseBuilder = body
    }

    override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji
            label = this@PublicInteractionButton.label
        }
    }

    override suspend fun call(event: ButtonInteractionCreateEvent): Unit = withLock {
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
        } catch (e: DiscordRelayedException) {
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
        context.respond { settings.errorResponseBuilder(this, message) }
    }
}
