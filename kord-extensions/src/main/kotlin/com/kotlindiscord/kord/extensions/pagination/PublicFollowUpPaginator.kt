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
import dev.kord.core.behavior.interaction.followup.edit
import dev.kord.core.behavior.interaction.response.FollowupPermittingInteractionResponseBehavior
import dev.kord.core.behavior.interaction.response.createPublicFollowup
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.interaction.followup.PublicFollowupMessage
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.embed
import dev.kord.rest.builder.message.modify.embed
import java.util.*

/**
 * Class representing a button-based paginator that operates by creating and editing a follow-up message for the
 * given public interaction response.
 *
 * @param interaction Interaction response behaviour to work with.
 */
public class PublicFollowUpPaginator(
	pages: Pages,
	owner: UserBehavior? = null,
	timeoutSeconds: Long? = null,
	keepEmbed: Boolean = true,
	switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	mutator: PageTransitionCallback? = null,
	bundle: String? = null,
	locale: Locale? = null,

	public val interaction: FollowupPermittingInteractionResponseBehavior,
) : BaseButtonPaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, mutator, bundle, locale) {
    /** Follow-up interaction to use for this paginator's embeds. Will be created by [send]. **/
    public var embedInteraction: PublicFollowupMessage? = null

    override suspend fun send() {
        if (embedInteraction == null) {
            setup()

            embedInteraction = interaction.createPublicFollowup {
                embed { applyPage() }

                with(this@PublicFollowUpPaginator.components) {
                    this@createPublicFollowup.applyToMessage()
                }
            }
        } else {
            updateButtons()

            embedInteraction!!.edit {
                embed { applyPage() }

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
                embed { applyPage() }

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
    interaction: FollowupPermittingInteractionResponseBehavior
): PublicFollowUpPaginator = PublicFollowUpPaginator(
    pages = builder.pages,
    owner = builder.owner,
    timeoutSeconds = builder.timeoutSeconds,
    keepEmbed = builder.keepEmbed,
	mutator = builder.mutator,
    bundle = builder.bundle,
    locale = builder.locale,
    interaction = interaction,

    switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)
