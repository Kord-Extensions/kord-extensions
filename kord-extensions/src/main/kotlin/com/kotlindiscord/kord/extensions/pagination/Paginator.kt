package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.pagination.pages.Page
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import com.kotlindiscord.kord.extensions.utils.respond
import com.kotlindiscord.kord.extensions.utils.waitFor
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.event.Event
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.util.*

private const val WRONG_TYPE = "Wrong event type!"

/** Emoji used to jump to the first page. **/
public val FIRST_PAGE_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\u23EE")

/** Emoji used to jump to the previous page. **/
public val LEFT_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\u2B05")

/** Emoji used to jump to the next page. **/
public val RIGHT_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\u27A1")

/** Emoji used to jump to the last page. **/
public val LAST_PAGE_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\u23ED")

/** Emoji used to destroy the paginator. **/
public val DELETE_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\u274C")

/** Group switch emoji, counter-clockwise arrows icon. **/
public val SWITCH_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\uD83D\uDD04")

/** Group switch emoji, information icon. **/
public val EXPAND_EMOJI: ReactionEmoji = ReactionEmoji.Unicode("\u2139\uFE0F")

private val logger = KotlinLogging.logger {}

/**
 * Paginator for, well, pagination.
 *
 * This paginator is fairly extensible, supporting subclassing of this class, as well as the [Pages] and [Page]
 * classes. It's designed with page groups in mind, which means you can provide groups of pages that can be switched
 * between using a dedicated reaction.
 *
 * @param bot The bot object this paginator was created for
 * @param targetChannel The channel this paginator should be created within
 * @param targetMessage The message this paginator should be created in response to
 * @param pages Set of pages this paginator should paginate
 * @param pingInReply When [targetMessage] is provided, whether to ping the message author in the reply
 * @param owner Optional paginator owner, if you want to prevent other users from using the reactions
 * @param timeout Optional timeout, after which the paginator will be destroyed
 * @param keepEmbed Whether to keep the embed after the paginator is destroyed, `false` by default
 * @param switchEmoji If you have multiple groups, this is the emoji used to switch between them
 * @param locale Locale to use for translations
 */
public open class Paginator(
    public val bot: ExtensibleBot,
    public val pages: Pages,
    public val targetChannel: MessageChannelBehavior? = null,
    public val targetMessage: Message? = null,
    public val owner: User? = null,
    public val timeout: Long? = null,
    public val keepEmbed: Boolean = true,
    public val pingInReply: Boolean = true,
    public val switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    public val locale: Locale = bot.settings.i18nBuilder.defaultLocale
) {
    init {
        if (targetChannel == null && targetMessage == null) {
            throw IllegalArgumentException("Must provide either a target channel or target message")
        }
    }

    /** Basic emojis that should be added to every paginator. **/
    public open val emojis: Array<ReactionEmoji> = arrayOf(
        FIRST_PAGE_EMOJI,
        LEFT_EMOJI,
        RIGHT_EMOJI,
        LAST_PAGE_EMOJI,
    )

    /** Reactions to add to the paginator after it's been sent. **/
    public open val reactions: MutableList<ReactionEmoji> = mutableListOf<ReactionEmoji>()

    /** Whether this paginator is currently active and processing events. **/
    public open var active: Boolean = true

    /** Currently-displayed page index. **/
    public var currentPageNum: Int = 0

    /** Currently-displayed page group. **/
    public var currentGroup: String = pages.defaultGroup

    /** Set of all page groups. **/
    public open var allGroups: List<String> = pages.groups.map { it.key }

    /** Currently-displayed page object. **/
    public open var currentPage: Page = pages.get(currentGroup, currentPageNum)

    /** Convenience function to send the current page to the channel, editing if a message is passed. **/
    public open suspend fun sendCurrentPage(message: MessageBehavior? = null): MessageBehavior {
        val groupEmoji = if (pages.groups.size > 1) {
            currentGroup
        } else {
            null
        }

        val builder = currentPage.build(
            bot,
            locale,
            currentPageNum,
            pages.size,
            groupEmoji,
            allGroups.indexOf(currentGroup),
            allGroups.size
        )

        return if (message != null) {
            message.edit { embed(builder) }
        } else if (targetChannel != null) {
            targetChannel.createEmbed(builder)
        } else if (targetMessage != null) {
            targetMessage.respond {
                embed(builder)

                if (!pingInReply) {
                    allowedMentions {}
                }
            }
        } else {
            throw IllegalArgumentException("Must provide either a target channel or target message")
        }
    }

    /** Send the embed to the channel given in the constructor. **/
    @KordPreview
    public open suspend fun send() {
        pages.validate()  // Will throw if there's a problem

        val message = sendCurrentPage()

        if (pages.size > 1) {
            reactions += emojis
        }

        if (pages.groups.size > 1) {
            reactions += switchEmoji
        }

        if (message.getChannelOrNull() !is DmChannel) {
            reactions += DELETE_EMOJI
        }

        if (reactions.isNotEmpty()) {
            if (reactions.size == 1 && reactions.first() == DELETE_EMOJI) {
                return  // No point in paginating this
            }

            reactions.forEach { message.addReaction(it) }

            val guildCondition: suspend Event.() -> Boolean = {
                this is ReactionAddEvent &&
                    message.id == this.messageId &&
                    this.userId != bot.kord.selfId &&
                    (owner == null || owner.id == this.userId) &&
                    active
            }

            val dmCondition: suspend Event.() -> Boolean = {
                when (this) {
                    is ReactionAddEvent -> message.id == this.messageId &&
                        this.userId != bot.kord.selfId &&
                        (owner == null || owner.id == this.userId) &&
                        active

                    is ReactionRemoveEvent -> message.id == this.messageId &&
                        this.userId != bot.kord.selfId &&
                        (owner == null || owner.id == this.userId) &&
                        active

                    else -> false
                }
            }

            while (true) {
                val condition = if (message.getChannelOrNull() is DmChannel) {
                    dmCondition
                } else {
                    guildCondition
                }

                val event = if (timeout != null) {
                    bot.kord.waitFor(timeout = timeout, condition = condition)
                } else {
                    bot.kord.waitFor(condition = condition)
                } ?: break

                processEvent(event)
            }

            if (timeout != null) {
                destroy(message)
            }
        } else {
            if (timeout != null && !keepEmbed) {
                delay(timeout)
                destroy(message)
            }
        }
    }

    /**
     * Paginator event handler.
     *
     * @param event [Event] to process.
     */
    public open suspend fun processEvent(event: Event) {
        val emoji = when (event) {
            is ReactionAddEvent -> event.emoji
            is ReactionRemoveEvent -> event.emoji

            else -> error(WRONG_TYPE)
        }

        val message = when (event) {
            is ReactionAddEvent -> event.message.asMessage()
            is ReactionRemoveEvent -> event.message.asMessage()

            else -> error(WRONG_TYPE)
        }

        val userId = when (event) {
            is ReactionAddEvent -> event.userId
            is ReactionRemoveEvent -> event.userId

            else -> error(WRONG_TYPE)
        }

        logger.debug { "Paginator received emoji ${emoji.name}" }

        val channel = message.getChannelOrNull()

        if (channel !is DmChannel) {
            message.deleteReaction(userId, emoji)
        }

        when (emoji) {
            FIRST_PAGE_EMOJI -> goToPage(message, 0)
            LEFT_EMOJI -> goToPage(message, currentPageNum - 1)
            RIGHT_EMOJI -> goToPage(message, currentPageNum + 1)
            LAST_PAGE_EMOJI -> goToPage(message, pages.size - 1)
            DELETE_EMOJI -> if (channel !is DmChannel) destroy(message)

            switchEmoji -> switchGroup(message)

            else -> return
        }
    }

    /** Convenience function to switch the currently displayed group. **/
    public open suspend fun switchGroup(message: MessageBehavior) {
        val current = currentGroup
        val nextIndex = allGroups.indexOf(current) + 1

        currentGroup = if (nextIndex >= allGroups.size) {
            allGroups.first()
        } else {
            allGroups[nextIndex]
        }

        currentPage = pages.get(currentGroup, currentPageNum)

        sendCurrentPage(message)
    }

    /**
     * Switch to another page in the current group.
     *
     * @param page Page number to display.
     */
    public open suspend fun goToPage(message: MessageBehavior, page: Int) {
        if (page == currentPageNum) {
            return
        }

        if (page < 0 || page > pages.size - 1) {
            return
        }

        currentPageNum = page
        currentPage = pages.get(currentGroup, currentPageNum)

        sendCurrentPage(message)
    }

    /**
     * Destroy the paginator.
     *
     * This will stop the paginator from processing events, and delete its message if [keepEmbed] is `false`.
     */
    public open suspend fun destroy(message: MessageBehavior) {
        if (!active) {
            return
        }

        if (!keepEmbed) {
            message.delete()
        } else {
            if (message.asMessage().getChannelOrNull() !is DmChannel) {
                message.deleteAllReactions()
            } else {
                reactions.forEach { message.deleteOwnReaction(it) }
            }
        }

        active = false
    }
}
