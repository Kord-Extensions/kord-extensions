package com.kotlindiscord.kord.extensions.modules.unsafe.types

import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.pagination.BaseButtonPaginator
import com.kotlindiscord.kord.extensions.pagination.EphemeralResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.PublicFollowUpPaginator
import com.kotlindiscord.kord.extensions.pagination.PublicResponsePaginator
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.Message
import dev.kord.core.entity.interaction.EphemeralFollowupMessage
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.message.create.EphemeralFollowupMessageCreateBuilder
import dev.kord.rest.builder.message.create.PublicFollowupMessageCreateBuilder
import dev.kord.rest.builder.message.modify.EphemeralInteractionResponseModifyBuilder
import dev.kord.rest.builder.message.modify.PublicInteractionResponseModifyBuilder
import java.util.*

/** Interface representing a generic, unsafe interaction action context. **/
@UnsafeAPI
public interface UnsafeInteractionContext {
    /** Response created by acknowledging the interaction. Generic. **/
    public val interactionResponse: InteractionResponseBehavior
}

/** Respond to the current interaction with a public followup. **/
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.respondPublic(
    builder: PublicFollowupMessageCreateBuilder.() -> Unit
): PublicFollowupMessage {
    return when (val interaction = interactionResponse) {
        is PublicInteractionResponseBehavior -> interaction.followUp(builder)
        is EphemeralInteractionResponseBehavior -> interaction.followUpPublic(builder)

        else -> error("Unsupported initial interaction response type - please report this.")
    }
}

/** Respond to the current interaction with an ephemeral followup, or throw if it isn't ephemeral. **/
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.respondEphemeral(
    builder: EphemeralFollowupMessageCreateBuilder.() -> Unit
): EphemeralFollowupMessage {
    val interaction = interactionResponse as? EphemeralInteractionResponseBehavior ?: error(
        "Initial interaction response is not public."
    )

    return interaction.followUpEphemeral(builder)
}

/**
 * Edit the current interaction's response, or throw if it isn't public.
 */
@Suppress("UseIfInsteadOfWhen")
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.editPublic(
    builder: PublicInteractionResponseModifyBuilder.() -> Unit
): Message {
    return when (val interaction = interactionResponse) {
        is PublicInteractionResponseBehavior -> interaction.edit(builder)

        else -> error("Initial interaction response was not public.")
    }
}

/**
 * Edit the current interaction's response, or throw if it isn't ephemeral.
 */
@Suppress("UseIfInsteadOfWhen")
@UnsafeAPI
public suspend inline fun UnsafeInteractionContext.editEphemeral(
    builder: EphemeralInteractionResponseModifyBuilder.() -> Unit
) {
    when (val interaction = interactionResponse) {
        is EphemeralInteractionResponseBehavior -> interaction.edit(builder)

        else -> error("Initial interaction response was not ephemeral.")
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

        else -> error("Initial interaction response was not public.")
    }
}
