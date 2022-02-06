/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.components.callbacks.EphemeralButtonCallback
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.types.respond
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder

public typealias InitialEphemeralButtonResponseBuilder =
    (suspend InteractionResponseCreateBuilder.(ButtonInteractionCreateEvent) -> Unit)?

/** Class representing an ephemeral-only interaction button. **/
public open class EphemeralInteractionButton(
    timeoutTask: Task?
) : InteractionButtonWithAction<EphemeralInteractionButtonContext>(timeoutTask) {
    /** Button style - anything but Link is valid. **/
    public open var style: ButtonStyle = ButtonStyle.Primary

    /** @suppress Initial response builder. **/
    public open var initialResponseBuilder: InitialEphemeralButtonResponseBuilder = null

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialEphemeralButtonResponseBuilder) {
        initialResponseBuilder = body
    }

    override fun useCallback(id: String) {
        action {
            val callback: EphemeralButtonCallback = callbackRegistry.getOfTypeOrNull(id)
                ?: error("Callback \"$id\" is either missing or is the wrong type.")

            callback.call(this)
        }

        check {
            val callback: EphemeralButtonCallback = callbackRegistry.getOfTypeOrNull(id)
                ?: error("Callback \"$id\" is either missing or is the wrong type.")

            passed = callback.runChecks(event)
        }
    }

    override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji
            label = this@EphemeralInteractionButton.label

            disabled = this@EphemeralInteractionButton.disabled
        }
    }

    override suspend fun call(event: ButtonInteractionCreateEvent): Unit = withLock {
        super.call(event)

        try {
            if (!runChecks(event)) {
                return@withLock
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral {
                settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
            }

            return@withLock
        }

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondEphemeral { initialResponseBuilder!!(event) }
        } else {
            if (!deferredAck) {
                event.interaction.acknowledgeEphemeral()
            } else {
                event.interaction.acknowledgeEphemeralDeferredMessageUpdate()
            }
        }

        val context = EphemeralInteractionButtonContext(this, event, response)

        context.populate()

        firstSentryBreadcrumb(context, this)

        try {
            checkBotPerms(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))

            return@withLock
        }

        try {
            body(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason, FailureReason.RelayedFailure(e))
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

    override suspend fun respondText(
        context: EphemeralInteractionButtonContext,
        message: String,
        failureType: FailureReason<*>
    ) {
        context.respond { settings.failureResponseBuilder(this, message, failureType) }
    }
}
