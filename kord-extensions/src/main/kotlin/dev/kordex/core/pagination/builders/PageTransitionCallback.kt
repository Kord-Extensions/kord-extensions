/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination.builders

import dev.kord.rest.builder.message.EmbedBuilder
import dev.kordex.core.pagination.BasePaginator
import dev.kordex.core.pagination.pages.Page

public typealias PageMutator = suspend EmbedBuilder.(page: Page) -> Unit
public typealias PaginatorMutator = suspend BasePaginator.() -> Unit

/** Builder containing callbacks used to modify paginators and their page content. **/
public class PageTransitionCallback {
	/** @suppress Variable storing the page mutator. **/
	public var pageMutator: PageMutator? = null

	/** @suppress Variable storing the paginator mutator. **/
	public var paginatorMutator: PaginatorMutator? = null

	/**
	 * Set the page mutator callback.
	 *
	 * Called just after we apply the page's embed builder, and just before the page modifies the embed's footer.
	 */
	public fun page(body: PageMutator) {
		pageMutator = body
	}

	/**
	 * Set the paginator mutator callback.
	 *
	 * Called just after we build a page embed, and just before that page is sent on Discord.
	 */
	public fun paginator(body: PaginatorMutator) {
		paginatorMutator = body
	}
}
