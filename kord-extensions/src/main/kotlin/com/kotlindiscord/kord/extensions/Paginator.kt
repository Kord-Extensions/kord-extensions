package com.kotlindiscord.kord.extensions

import com.gitlab.kordlib.common.annotation.KordPreview
import com.gitlab.kordlib.core.behavior.channel.MessageChannelBehavior
import com.gitlab.kordlib.core.behavior.channel.createEmbed
import com.gitlab.kordlib.core.behavior.edit
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.ReactionEmoji
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.event.message.ReactionAddEvent
import com.gitlab.kordlib.rest.builder.message.EmbedBuilder
import kotlinx.coroutines.delay
import mu.KotlinLogging

private val FIRST_PAGE_EMOJI = ReactionEmoji.Unicode("\u23EE")
private val LEFT_EMOJI = ReactionEmoji.Unicode("\u2B05")
private val RIGHT_EMOJI = ReactionEmoji.Unicode("\u27A1")
private val LAST_PAGE_EMOJI = ReactionEmoji.Unicode("\u23ED")
private val DELETE_EMOJI = ReactionEmoji.Unicode("\u274C")

private val EMOJIS = arrayOf(FIRST_PAGE_EMOJI, LEFT_EMOJI, RIGHT_EMOJI, LAST_PAGE_EMOJI, DELETE_EMOJI)

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
    /** Message containing the embed. **/
    open var message: Message? = null

    /** Current page of the paginator. **/
    open var currentPage: Int = 0

    /** Whether the paginator still processes reaction events. **/
    open var doesProcessEvents: Boolean = true

    /** Send the embed to the channel given in the constructor. **/
    @KordPreview
    open suspend fun send() {
        val myFooter = EmbedBuilder.Footer()
        myFooter.text = "Page 1/${pages.size}"

        message = channel.createEmbed {
            title = name
            description = pages[0]
            footer = myFooter
        }

        EMOJIS.forEach { message!!.addReaction(it) }

        while (true) {
            val handler: suspend ReactionAddEvent.() -> Boolean = {
                message.id == this.messageId &&
                    this.userId != bot.kord.selfId &&
                    (owner == null || owner.id == this.userId) &&
                    doesProcessEvents
            }

            val event = if (timeout > 0) {
                bot.kord.waitFor<ReactionAddEvent>(timeout = timeout, condition = handler)
            } else {
                bot.kord.waitFor<ReactionAddEvent>(condition = handler)
            } ?: break

            processEvent(event)
        }

        if (timeout > 0) {
            delay(timeout)
            destroy()
        }
    }

    /**
     * Paginator [ReactionAddEvent] handler.
     *
     * @param event [ReactionAddEvent] to process.
     */
    open suspend fun processEvent(event: ReactionAddEvent) {
        logger.debug { "Paginator received emoji ${event.emoji.name}" }
        event.message.deleteReaction(event.userId, event.emoji)

        when (event.emoji.name) {
            FIRST_PAGE_EMOJI.name -> goToPage(0)
            LEFT_EMOJI.name -> goToPage(currentPage - 1)
            RIGHT_EMOJI.name -> goToPage(currentPage + 1)
            LAST_PAGE_EMOJI.name -> goToPage(pages.size - 1)
            DELETE_EMOJI.name -> destroy()
            else -> return
        }
    }

    /** Display the provided page number.
     *
     * @param page Page number to display.
     */
    open suspend fun goToPage(page: Int) {
        if (page == currentPage) {
            return
        }
        if (page < 0 || page > pages.size - 1) {
            return
        }

        currentPage = page

        val myFooter = EmbedBuilder.Footer()
        myFooter.text = "Page ${page + 1}/${pages.size}"

        message?.edit {
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
    open suspend fun destroy() {
        if (!keepEmbed) {
            message?.delete()
        } else {
            message?.deleteAllReactions()
        }
        doesProcessEvents = false
    }
}
