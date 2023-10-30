/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.channel.InitialEphemeralSelectMenuResponseBuilder
import com.kotlindiscord.kord.extensions.types.FailureReason
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import com.kotlindiscord.kord.extensions.utils.getLocale
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.annotation.KordUnsafe
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Class representing a public-only select (dropdown) menu. **/
public abstract class PublicSelectMenu<C, M : ModalForm>(
    timeoutTask: Task?,
    public override val modal: (() -> M)? = null,
    /** The initial response builder, omit to ack instead. **/
    public open var initialResponseBuilder: InitialEphemeralSelectMenuResponseBuilder = null,
) : SelectMenu<C, M>(timeoutTask) where C : SelectMenuContext, C : PublicInteractionContext {

    /** Call this to open with a response, omit it to ack instead. **/
    public fun initialResponse(body: InitialEphemeralSelectMenuResponseBuilder) {
        initialResponseBuilder = body
    }

    override fun validate() {
        super.validate()

        if (modal != null && initialResponseBuilder != null) {
            error("You may not provide a modal builder and an initial response - pick one, not both.")
        }
    }

    /** Function to create the context for the select menu. **/
    public abstract fun createContext(
        event: SelectMenuInteractionCreateEvent,
        interactionResponse: PublicMessageInteractionResponseBehavior,
        cache: MutableStringKeyedMap<Any>,
    ): C

    @OptIn(KordUnsafe::class)
    @Suppress("TooGenericExceptionCaught")
    override suspend fun call(event: SelectMenuInteractionCreateEvent): Unit = withLock {
        val cache: MutableStringKeyedMap<Any> = mutableMapOf()

        super.call(event)

        try {
            if (!runChecks(event, cache)) {
                return@withLock
            }
        } catch (e: DiscordRelayedException) {
            event.interaction.respondEphemeral {
                settings.failureResponseBuilder(this, e.reason, FailureReason.ProvidedCheckFailure(e))
            }

            return@withLock
        }

        val modalObj = modal?.invoke()

        val response = if (initialResponseBuilder != null) {
            event.interaction.respondPublic { initialResponseBuilder!!(event) }
        } else if (modalObj != null) {
            componentRegistry.register(modalObj)

            val locale = event.getLocale()

            event.interaction.modal(
                modalObj.translateTitle(locale, bundle),
                modalObj.id
            ) {
                modalObj.applyToBuilder(this, event.getLocale(), bundle)
            }

            modalObj.awaitCompletion {
                componentRegistry.unregisterModal(modalObj)

                if (!deferredAck) {
                    it?.deferPublicResponseUnsafe()
                } else {
                    it?.deferPublicMessageUpdate()
                }
            } ?: return@withLock
        } else {
            if (!deferredAck) {
                event.interaction.deferPublicResponseUnsafe()
            } else {
                event.interaction.deferPublicMessageUpdate()
            }
        }

        val context = createContext(event, response, cache)

        context.populate()

        firstSentryBreadcrumb(context, this)

        try {
            checkBotPerms(context)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason, FailureReason.OwnPermissionsCheckFailure(e))

            return@withLock
        }

        try {
            body(context, modalObj)
        } catch (e: DiscordRelayedException) {
            respondText(context, e.reason, FailureReason.RelayedFailure(e))
        } catch (t: Throwable) {
            handleError(context, t, this)
        }
    }

    override suspend fun respondText(
        context: C,
        message: String,
        failureType: FailureReason<*>,
    ) {
        context.respond { settings.failureResponseBuilder(this, message, failureType) }
    }
}
