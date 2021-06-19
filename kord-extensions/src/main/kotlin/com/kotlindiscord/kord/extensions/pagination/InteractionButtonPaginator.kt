@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.PublicFollowupMessage
import dev.kord.rest.builder.interaction.embed
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
    switchEmoji: ReactionEmoji.Unicode = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    locale: Locale? = null,
    public val parentContext: SlashCommandContext<*>,
) : BaseButtonPaginator(extension, pages, owner, timeoutSeconds, keepEmbed, switchEmoji, locale) {
    init {
        if (parentContext.isEphemeral == true) {
            error("Paginators cannot operate with ephemeral interactions.")
        }
    }

    override var components: Components = Components(extension, parentContext)

    /** Follow-up message containing all of the buttons. **/
    public var embedInteraction: PublicFollowupMessage? = null

    override suspend fun setup() {
        components.onTimeout {
            destroy()
        }

        // Add navigation buttons...
        firstPageButton = components.interactiveButton {
            deferredAck = true
            style = ButtonStyle.Secondary

            check(defaultCheck)
            emoji(FIRST_PAGE_EMOJI)

            action {
                goToPage(0)

                send()
            }
        }

        backButton = components.interactiveButton {
            deferredAck = true
            style = ButtonStyle.Secondary

            check(defaultCheck)
            emoji(LEFT_EMOJI)

            action {
                previousPage()

                send()
            }
        }

        nextButton = components.interactiveButton {
            deferredAck = true
            style = ButtonStyle.Secondary

            check(defaultCheck)
            emoji(RIGHT_EMOJI)

            action {
                nextPage()

                send()
            }
        }

        lastPageButton = components.interactiveButton {
            deferredAck = true
            style = ButtonStyle.Secondary

            check(defaultCheck)
            emoji(LAST_PAGE_EMOJI)

            action {
                goToPage(pages.size - 1)

                send()
            }
        }

        // Add the destroy button
        components.interactiveButton(LAST_ROW) {
            deferredAck = true

            check(defaultCheck)
            emoji(DELETE_EMOJI)

            action {
                destroy()
            }
        }

        if (pages.groups.size > 1) {
            // Add the group switch button
            components.interactiveButton(LAST_ROW) {
                deferredAck = true

                check(defaultCheck)
                emoji(switchEmoji)

                action {
                    nextGroup()

                    send()
                }
            }
        }

        components.sortIntoRows()
        updateButtons()
    }

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

    override suspend fun nextGroup() {
        val current = currentGroup
        val nextIndex = allGroups.indexOf(current) + 1

        currentGroup = if (nextIndex >= allGroups.size) {
            allGroups.first()
        } else {
            allGroups[nextIndex]
        }

        currentPage = pages.get(currentGroup, currentPageNum)

        send()
    }

    override suspend fun goToPage(page: Int) {
        if (page == currentPageNum) {
            return
        }

        if (page < 0 || page > pages.size - 1) {
            return
        }

        currentPageNum = page
        currentPage = pages.get(currentGroup, currentPageNum)

        send()
    }

    override suspend fun destroy() {
        if (!active) {
            return
        }

        active = false
        components.stop()

        if (!keepEmbed) {
            embedInteraction!!.delete()
        } else {
            embedInteraction!!.edit {
                embed(embedBuilder)
            }
        }
    }
}
