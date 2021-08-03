@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.embed
import java.util.*

/**
 * Class representing a button-based paginator that operates on public-acked interactions. Essentially, use this with
 * slash commands.
 *
 * @param parentContext Parent slash command context to be worked with.
 */
public class InteractionButtonPaginator(
    extension: Extension,
    pages: Pages,
    owner: User? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,

    public val parentContext: SlashCommandContext<*>,
) : BaseButtonPaginator(extension, pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    init {
        if (parentContext.isEphemeral == true) {
            error("Paginators cannot operate with ephemeral interactions.")
        }
    }

    override var components: Components = Components(extension, parentContext)

    /** Follow-up message containing all of the buttons. **/
    public var embedInteraction: PublicFollowupMessage? = null

    override suspend fun send() {
        components.stop()

        if (embedInteraction == null) {
            setup()

            embedInteraction = parentContext.publicFollowUp {
                embed(embedBuilder)

                with(this@InteractionButtonPaginator.components) {
                    this@publicFollowUp.setup(timeoutSeconds)
                }
            }
        } else {
            updateButtons()

            embedInteraction!!.edit {
                embed(embedBuilder)

                with(this@InteractionButtonPaginator.components) {
                    this@edit.setup(timeoutSeconds)
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
            embedInteraction!!.delete()
        } else {
            embedInteraction!!.edit {
                embed(embedBuilder)

                this.components = mutableListOf()
            }
        }

        runTimeoutCallbacks()
        components.stop()
    }
}

/** Convenience function for creating an interaction button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
public fun InteractionButtonPaginator(
    builder: PaginatorBuilder,
    parentContext: SlashCommandContext<*>
): InteractionButtonPaginator = InteractionButtonPaginator(
    extension = builder.extension,
    pages = builder.pages,
    owner = builder.owner,
    timeoutSeconds = builder.timeoutSeconds,
    keepEmbed = builder.keepEmbed,
    bundle = builder.bundle,
    locale = builder.locale,
    parentContext = parentContext,

    switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
)
