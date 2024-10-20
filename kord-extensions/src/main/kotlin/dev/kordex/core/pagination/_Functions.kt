/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.FollowupPermittingInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kordex.core.i18n.EMPTY_KEY
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.pagination.builders.PaginatorBuilder
import java.util.*

/** Create a paginator that edits the original interaction. **/
public inline fun PublicMessageInteractionResponseBehavior.editingPaginator(
	defaultGroup: Key = EMPTY_KEY,
	locale: Locale? = null,
	builder: (PaginatorBuilder).() -> Unit,
): PublicResponsePaginator {
	val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

	builder(pages)

	return PublicResponsePaginator(pages, this)
}

/** Create a paginator that creates a follow-up message, and edits that. **/
public inline fun FollowupPermittingInteractionResponseBehavior.respondingPaginator(
	defaultGroup: Key = EMPTY_KEY,
	locale: Locale? = null,
	builder: (PaginatorBuilder).() -> Unit,
): PublicFollowUpPaginator {
	val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

	builder(pages)

	return PublicFollowUpPaginator(pages, this)
}

/**
 * Create a paginator that edits the original interaction. This is the only option for an ephemeral interaction, as
 * it's impossible to edit an ephemeral follow-up.
 */
public inline fun EphemeralMessageInteractionResponseBehavior.editingPaginator(
	defaultGroup: Key = EMPTY_KEY,
	locale: Locale? = null,
	builder: (PaginatorBuilder).() -> Unit,
): EphemeralResponsePaginator {
	val pages = PaginatorBuilder(locale = locale, defaultGroup = defaultGroup)

	builder(pages)

	return EphemeralResponsePaginator(pages, this)
}
