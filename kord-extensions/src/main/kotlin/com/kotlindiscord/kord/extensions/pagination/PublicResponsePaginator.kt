/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.pagination.builders.PageTransitionCallback
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.ReactionEmoji
import java.util.*

/**
 * Class representing a button-based paginator that operates by editing the given public interaction response.
 *
 * @param interaction Interaction response behaviour to work with.
 */
public class PublicResponsePaginator(
	pages: Pages,
	chunkedPages: Int = 1,
	owner: UserBehavior? = null,
	timeoutSeconds: Long? = null,
	keepEmbed: Boolean = true,
	switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	mutator: PageTransitionCallback? = null,
	bundle: String? = null,
	locale: Locale? = null,

	public val interaction: PublicMessageInteractionResponseBehavior,
) : BaseButtonPaginator(pages, chunkedPages, owner, timeoutSeconds, keepEmbed, switchEmoji, mutator, bundle, locale) {
	/** Whether this paginator has been set up for the first time. **/
	public var isSetup: Boolean = false

	override suspend fun send() {
		if (!isSetup) {
			isSetup = true

			setup()
		} else {
			updateButtons()
		}

		interaction.edit {
			applyPage()

			with(this@PublicResponsePaginator.components) {
				this@edit.applyToMessage()
			}
		}
	}

	override suspend fun destroy() {
		if (!active) {
			return
		}

		active = false

		interaction.edit {
			applyPage()

			this.components = mutableListOf()
		}

		super.destroy()
	}
}

/** Convenience function for creating an interaction button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
public fun PublicResponsePaginator(
	builder: PaginatorBuilder,
	interaction: PublicMessageInteractionResponseBehavior,
): PublicResponsePaginator = PublicResponsePaginator(
	pages = builder.pages,
	chunkedPages = builder.chunkedPages,
	owner = builder.owner,
	timeoutSeconds = builder.timeoutSeconds,
	keepEmbed = builder.keepEmbed,
	mutator = builder.mutator,
	bundle = builder.bundle,
	locale = builder.locale,
	interaction = interaction,

	switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)
