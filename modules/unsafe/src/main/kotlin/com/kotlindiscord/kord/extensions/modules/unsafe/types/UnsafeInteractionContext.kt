@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.unsafe.types

import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.EphemeralResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.core.event.interaction.ApplicationInteractionCreateEvent
import dev.kord.rest.builder.message.create.FollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kord.rest.builder.message.modify.InteractionResponseModifyBuilder
import java.util.*

/** Interface representing a generic, unsafe interaction action context. **/
@UnsafeAPI
public interface UnsafeInteractionContext {
    /** Response created by acknowledging the interaction. Generic. **/
    public var interactionResponse: InteractionResponseBehavior?

    /** Original interaction event object, for manual acks. **/
    public val event: ApplicationInteractionCreateEvent
}

/** Send an ephemeral ack, if the interaction hasn't been acknowledged yet. **/
@UnsafeAPI
public suspend fun UnsafeInteractionContext.ackEphemeral(
    builder: (suspend InteractionResponseCreateBuilder.() -> Unit)? = null
): EphemeralInteractionResponseBehavior {
    if (interactionResponse != null) {
        error("The interaction has already been acknowledged.")
    }

    interactionResponse = if (builder == null) {
        event.interaction.acknowledgeEphemeral()
    } else {
        event.interaction.respondEphemeral { builder() }
    }

    return interactionResponse as EphemeralInteractionResponseBehavior
}

/** Send a public ack, if the interaction hasn't been acknowledged yet. **/
@UnsafeAPI
public suspend fun UnsafeInteractionContext.ackPublic(
    builder: (suspend InteractionResponseCreateBuilder.() -> Unit)? = null
): PublicInteractionResponseBehavior {
    if (interactionResponse != null) {
        error("The interaction has already been acknowledged.")
    }

    interactionResponse = if (builder == null) {
        event.interaction.acknowledgePublic()
    } else {
        event.interaction.respondPublic { builder() }
    }

    return interactionResponse as PublicInteractionResponseBehavior
}

/** Respond to the current interaction with an ephemeral followup, or throw if it isn't ephemeral. **/
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.respondEphemeral(
    builder: FollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage {
    return when (val interaction = interactionResponse) {
        is InteractionResponseBehavior -> interaction.ephemeralFollowup(builder)

        null -> error("Acknowledge the interaction before trying to follow-up.")
        else -> error("Unsupported initial interaction response type $interaction - please report this.")
    }
}

/** Respond to the current interaction with a public followup. **/
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.respondPublic(
    builder: FollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage {
    return when (val interaction = interactionResponse) {
        is InteractionResponseBehavior -> interaction.followUp { builder() }

        null -> error("Acknowledge the interaction before trying to follow-up.")
        else -> error("Unsupported initial interaction response type $interaction - please report this.")
    }
}

/**
 * Edit the current interaction's response, or throw if it isn't public.
 */
@Suppress("UseIfInsteadOfWhen")
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.edit(
    builder: InteractionResponseModifyBuilder.() -> Unit
) {
    return when (val interaction = interactionResponse) {
        is InteractionResponseBehavior -> interaction.edit(builder)

        null -> error("Acknowledge the interaction before trying to edit it.")
        else -> error("Unsupported initial interaction response type $interaction - please report this.")
    }
}

/** Create a paginator that edits the original interaction. **/
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.editingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): BaseButtonPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return when (val interaction = interactionResponse) {
        is PublicInteractionResponseBehavior -> PublicResponsePaginator(pages, interaction)
        is EphemeralInteractionResponseBehavior -> EphemeralResponsePaginator(pages, interaction)

        null -> error("Acknowledge the interaction before trying to edit it.")
        else -> error("Unsupported initial interaction response type - please report this.")
    }
}

/** Create a paginator that creates a follow-up message, and edits that. **/
@Suppress("UseIfInsteadOfWhen")
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.respondingPaginator(
    defaultGroup: String = "",
    locale: Locale? = null,
    builder: (PaginatorBuilder).() -> Unit
): BaseButtonPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return when (val interaction = interactionResponse) {
        is PublicInteractionResponseBehavior -> PublicFollowUpPaginator(pages, interaction)

        null -> error("Acknowledge the interaction before trying to follow-up.")
        else -> error("Initial interaction response was not public.")
    }
}
