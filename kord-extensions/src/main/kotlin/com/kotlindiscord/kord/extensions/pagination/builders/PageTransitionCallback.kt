package com.kotlindiscord.kord.extensions.pagination.builders

import com.kotlindiscord.kord.extensions.pagination.BasePaginator
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import dev.kord.rest.builder.message.EmbedBuilder

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
