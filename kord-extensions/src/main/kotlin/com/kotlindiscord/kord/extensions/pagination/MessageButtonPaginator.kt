@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import java.util.*

/**
 * Class representing a button-based paginator that operates on standard messages.
 *
 * @param targetMessage Target message to reply to, overriding [targetChannel].
 * @param targetChannel Target channel to send the paginator to, if [targetMessage] isn't provided.
 */
public class MessageButtonPaginator(
    extension: Extension,
    pages: Pages,
    owner: User? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji.Unicode = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    locale: Locale? = null,
    public val targetChannel: MessageChannelBehavior? = null,
    public val targetMessage: Message? = null,
) : BaseButtonPaginator(extension, pages, owner, timeoutSeconds, keepEmbed, switchEmoji, locale) {
    init {
        if (targetChannel == null && targetMessage == null) {
            throw IllegalArgumentException("Must provide either a target channel or target message")
        }
    }

    override var components: Components = Components(extension)

    /** Specific channel to send the paginator to. **/
    public val channel: MessageChannelBehavior = targetMessage?.channel ?: targetChannel!!

    /** Message containing the paginator. **/
    public var message: Message? = null

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

        if (message == null) {
            setup()

            message = channel.createMessage {
                this.messageReference = targetMessage?.id
                embed(embedBuilder)

                with(this@MessageButtonPaginator.components) {
                    this@createMessage.setup(timeoutSeconds)
                }
            }
        } else {
            updateButtons()

            message!!.edit {
                embed(embedBuilder)

                with(this@MessageButtonPaginator.components) {
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
            message!!.delete()
        } else {
            message!!.edit {
                embed(embedBuilder)
            }
        }
    }
}
