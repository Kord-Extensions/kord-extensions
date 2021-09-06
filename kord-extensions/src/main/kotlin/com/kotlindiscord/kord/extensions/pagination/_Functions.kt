package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import java.util.*

public suspend inline fun PublicInteractionResponseBehavior.editingPaginator(
    locale: Locale? = null,
    defaultGroup: String = "",
    builder: (PaginatorBuilder).() -> Unit
): PublicResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return PublicResponsePaginator(pages, this)
}

public suspend inline fun PublicInteractionResponseBehavior.respondingPaginator(
    locale: Locale? = null,
    defaultGroup: String = "",
    builder: (PaginatorBuilder).() -> Unit
): PublicFollowUpPaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return PublicFollowUpPaginator(pages, this)
}

public suspend inline fun EphemeralInteractionResponseBehavior.editingPaginator(
    locale: Locale? = null,
    defaultGroup: String = "",
    builder: (PaginatorBuilder).() -> Unit
): EphemeralResponsePaginator {
    val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

    builder(pages)

    return EphemeralResponsePaginator(pages, this)
}
