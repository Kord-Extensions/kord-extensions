package com.kotlindiscord.kord.extensions

import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.event.Event
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.rest.builder.message.EmbedBuilder
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
public open class Paginator(
    public val bot: ExtensibleBot,
    public val channel: MessageChannelBehavior,
    public val name: String,
    public val pages: List<String>,
    public val owner: User? = null,
    public val timeout: Long = -1L,
    public val keepEmbed: Boolean = false
) {
    /** Current page of the paginator. **/
    public open var currentPage: Int = 0

    /** Whether the paginator still processes reaction events. **/
    public open var doesProcessEvents: Boolean = true

    /** Send the embed to the channel given in the constructor. **/
    @KordPreview
    public open suspend fun send() {
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
    public open suspend fun processEvent(event: Event) {
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
    public open suspend fun goToPage(message: MessageBehavior, page: Int) {
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
    public open suspend fun destroy(message: MessageBehavior) {
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
