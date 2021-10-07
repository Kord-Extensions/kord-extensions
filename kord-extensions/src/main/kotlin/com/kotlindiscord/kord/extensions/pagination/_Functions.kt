package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import java.util.*

/** Create a paginator that edits the original interaction. **/
public suspend inline fun PublicInteractionResponseBehavior.editingPaginator(
    locale: Locale? = null,
    defaultGroup: String = "",
    builder: (PaginatorBuilder).() -> Unit
): ResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return ResponsePaginator(pages, this)
}

/** Create a paginator that creates a follow-up message, and edits that. **/
public suspend inline fun PublicInteractionResponseBehavior.respondingPaginator(
    locale: Locale? = null,
    defaultGroup: String = "",
    builder: (PaginatorBuilder).() -> Unit
): PublicFollowUpPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return PublicFollowUpPaginator(pages, this)
}

/**
 * Create a paginator that edits the original interaction. This is the only option for an ephemeral interaction, as
 * it's impossible to edit an ephemeral follow-up.
 */
public suspend inline fun EphemeralInteractionResponseBehavior.editingPaginator(
    locale: Locale? = null,
    defaultGroup: String = "",
    builder: (PaginatorBuilder).() -> Unit
): ResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return ResponsePaginator(pages, this)
}
