package com.kotlindiscord.kord.extensions

import com.gitlab.kordlib.common.annotation.KordPreview
import com.gitlab.kordlib.core.behavior.MessageBehavior
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.DmChannel
import com.gitlab.kordlib.core.event.Event
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.core.event.message.ReactionRemoveEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import com.kotlindiscord.kord.extensions.utils.waitFor
import kotlinx.coroutines.delay
import mu.KotlinLogging

private val FIRST_PAGE_EMOJI = ReactionEmoji.Unicode("\u23EE")
private val LEFT_EMOJI = ReactionEmoji.Unicode("\u2B05")
private val RIGHT_EMOJI = ReactionEmoji.Unicode("\u27A1")
private val LAST_PAGE_EMOJI = ReactionEmoji.Unicode("\u23ED")
private val DELETE_EMOJI = ReactionEmoji.Unicode("\u274C")

private val EMOJIS = arrayOf(FIRST_PAGE_EMOJI, LEFT_EMOJI, RIGHT_EMOJI, LAST_PAGE_EMOJI)

private val logger = KotlinLogging.logger {}

/**
 * Interactive embed with multiple pages using emoji reactions as user inputs.
 *
 * NOTE : `.send()` needs to be called in order for the paginator to be displayed.
 *
 * @param bot Current instance of the bot.
 * @param channel Channel to send the embed to.
 * @param name Title of the embed.
 * @param pages List of the embed pages.
 * @param owner Only user capable of interacting with the paginator. Everyone is able to if it is set to null.
 * @param timeout Milliseconds before automatically deleting the embed. Set to a negative value to disable it.
 * @param keepEmbed Keep the embed and only remove the reaction when the paginator is destroyed.
 */
open class Paginator(
    val bot: ExtensibleBot,
    val channel: MessageChannelBehavior,
    val name: String,
    val pages: List<String>,
    val owner: User? = null,
    val timeout: Long = -1L,
    val keepEmbed: Boolean = false
) {
    /** Current page of the paginator. **/
    open var currentPage: Int = 0

    /** Whether the paginator still processes reaction events. **/
    open var doesProcessEvents: Boolean = true

    /** Send the embed to the channel given in the constructor. **/
    @KordPreview
    open suspend fun send() {
        val myFooter = EmbedBuilder.Footer()

        myFooter.text = if (pages.size > 1) {
            "Page 1/${pages.size}"
        } else {
            "No further pages."
        }

        val message = channel.createEmbed {
            title = name
            description = pages[0]
            footer = myFooter
        }

        if (pages.size > 1) {
            EMOJIS.forEach { message.addReaction(it) }

            if (message.getChannelOrNull() !is DmChannel) {
                message.addReaction(DELETE_EMOJI)
            }

            val guildCondition: suspend Event.() -> Boolean = {
                this is ReactionAddEvent &&
                    message.id == this.messageId &&
                    this.userId != bot.kord.selfId &&
                    (owner == null || owner.id == this.userId) &&
                    doesProcessEvents
            }

            val dmCondition: suspend Event.() -> Boolean = {
                when (this) {
                    is ReactionAddEvent -> message.id == this.messageId &&
                        this.userId != bot.kord.selfId &&
                        (owner == null || owner.id == this.userId) &&
                        doesProcessEvents

                    is ReactionRemoveEvent -> message.id == this.messageId &&
                        this.userId != bot.kord.selfId &&
                        (owner == null || owner.id == this.userId) &&
                        doesProcessEvents

                    else -> false
                }
            }

            while (true) {
                val condition = if (message.getChannelOrNull() is DmChannel) {
                    dmCondition
                } else {
                    guildCondition
                }

                val event = if (timeout > 0) {
                    bot.kord.waitFor(timeout = timeout, condition = condition)
                } else {
                    bot.kord.waitFor(condition = condition)
                } ?: break

                processEvent(event)
            }

            if (timeout > 0) {
                destroy(message)
            }
        } else {
            if (timeout > 0 && !keepEmbed) {
                delay(timeout)
                destroy(message)
            }
        }
    }

    /**
     * Paginator [ReactionAddEvent] handler.
     *
     * @param event [ReactionAddEvent] to process.
     */
    open suspend fun processEvent(event: Event) {
        val emoji = when (event) {
            is ReactionAddEvent -> event.emoji
            is ReactionRemoveEvent -> event.emoji

            else -> error("Wrong event type!")
        }

        val message = when (event) {
            is ReactionAddEvent -> event.message.asMessage()
            is ReactionRemoveEvent -> event.message.asMessage()

            else -> error("Wrong event type!")
        }

        val userId = when (event) {
            is ReactionAddEvent -> event.userId
            is ReactionRemoveEvent -> event.userId

            else -> error("Wrong event type!")
        }

        logger.debug { "Paginator received emoji ${emoji.name}" }

        val channel = message.getChannelOrNull()

        if (channel !is DmChannel) {
            message.deleteReaction(userId, emoji)
        }

        when (emoji.name) {
            FIRST_PAGE_EMOJI.name -> goToPage(message, 0)
            LEFT_EMOJI.name -> goToPage(message, currentPage - 1)
            RIGHT_EMOJI.name -> goToPage(message, currentPage + 1)
            LAST_PAGE_EMOJI.name -> goToPage(message, pages.size - 1)
            DELETE_EMOJI.name -> if (channel !is DmChannel) destroy(message)
            else -> return
        }
    }

    /** Display the provided page number.
     *
     * @param page Page number to display.
     */
    open suspend fun goToPage(message: MessageBehavior, page: Int) {
        if (page == currentPage) {
            return
        }
        if (page < 0 || page > pages.size - 1) {
            return
        }

        currentPage = page

        val myFooter = EmbedBuilder.Footer()
        myFooter.text = "Page ${page + 1}/${pages.size}"

        message.edit {
            embed {
                title = name
                description = pages[page]
                footer = myFooter
            }
        }
    }

    /** Destroy the paginator.
     *
     * This will make it stops receive [ReactionAddEvent] and will delete the embed if `keepEmbed` is set to true,
     * or will delete all the reactions if it is set to false.
     */
    open suspend fun destroy(message: MessageBehavior) {
        if (!keepEmbed) {
            message.delete()
        } else {
            if (message.asMessage().getChannelOrNull() !is DmChannel) {
                message.deleteAllReactions()
            } else {
                EMOJIS.forEach { message.deleteOwnReaction(it) }
            }
        }
        doesProcessEvents = false
    }
}
