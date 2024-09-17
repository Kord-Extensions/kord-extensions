/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.FollowupPermittingInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kordex.core.pagination.builders.PageTransitionCallback
import dev.kordex.core.pagination.builders.PaginatorBuilder
import dev.kordex.core.pagination.pages.Pages
import java.util.*

/**
 * Class representing a button-based paginator that operates by creating and editing a follow-up message for the
 * given public interaction response.
 *
 * @param interaction Interaction response behaviour to work with.
 */
public class PublicFollowUpPaginator(
	pages: Pages,
	chunkedPages: Int = 1,
	owner: UserBehavior? = null,
	timeoutSeconds: Long? = null,
	keepEmbed: Boolean = true,
	switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	mutator: PageTransitionCallback? = null,
	locale: Locale? = null,

	public val interaction: FollowupPermittingInteractionResponseBehavior,
) : BaseButtonPaginator(pages, chunkedPages, owner, timeoutSeconds, keepEmbed, switchEmoji, mutator, locale) {
	/** Follow-up interaction to use for this paginator's embeds. Will be created by [send]. **/
	public var embedInteraction: PublicFollowupMessage? = null

	override suspend fun send() {
		if (embedInteraction == null) {
			setup()

			embedInteraction = interaction.createPublicFollowup {
				applyPage()

				with(this@PublicFollowUpPaginator.components) {
					this@createPublicFollowup.applyToMessage()
				}
			}
		} else {
			updateButtons()

			embedInteraction!!.edit {
				applyPage()

				with(this@PublicFollowUpPaginator.components) {
					this@edit.applyToMessage()
				}
			}
		}
	}

	override suspend fun destroy() {
		if (!active) {
			return
		}

		active = false

		if (!keepEmbed) {
			embedInteraction?.delete()
		} else {
			embedInteraction?.edit {
				applyPage()

				this.components = mutableListOf()
			}
		}

		super.destroy()
	}
}

/** Convenience function for creating an interaction button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
public fun PublicFollowUpPaginator(
	builder: PaginatorBuilder,
	interaction: FollowupPermittingInteractionResponseBehavior,
): PublicFollowUpPaginator = PublicFollowUpPaginator(
	pages = builder.pages,
	chunkedPages = builder.chunkedPages,
	owner = builder.owner,
	timeoutSeconds = builder.timeoutSeconds,
	keepEmbed = builder.keepEmbed,
	mutator = builder.mutator,
	locale = builder.locale,
	interaction = interaction,

	switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)
