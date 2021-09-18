@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.pagination

import com.kotlindiscord.kord.extensions.pagination.builders.PaginatorBuilder
import com.kotlindiscord.kord.extensions.pagination.pages.Pages
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.create.allowedMentions
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.allowedMentions
import dev.kord.rest.builder.message.modify.embed
import java.util.*

/**
 * Class representing a button-based paginator that operates on standard messages.
 *
 * @param pingInReply Whether to ping the author of [targetMessage] in reply.
 * @param targetMessage Target message to reply to, overriding [targetChannel].
 * @param targetChannel Target channel to send the paginator to, if [targetMessage] isn't provided.
 */
public class MessageButtonPaginator(
    pages: Pages,
    owner: UserBehavior? = null,
    timeoutSeconds: Long? = null,
    keepEmbed: Boolean = true,
    switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    bundle: String? = null,
    locale: Locale? = null,

    public val pingInReply: Boolean = true,
    public val targetChannel: MessageChannelBehavior? = null,
    public val targetMessage: Message? = null,
) : BaseButtonPaginator(pages, owner, timeoutSeconds, keepEmbed, switchEmoji, bundle, locale) {
    init {
        if (targetChannel == null && targetMessage == null) {
            throw IllegalArgumentException("Must provide either a target channel or target message")
        }
    }

    /** Specific channel to send the paginator to. **/
    public val channel: MessageChannelBehavior = targetMessage?.channel ?: targetChannel!!

    /** Message containing the paginator. **/
    public var message: Message? = null

    override suspend fun send() {
        if (message == null) {
            setup()

            message = channel.createMessage {
                this.messageReference = targetMessage?.id

                allowedMentions { repliedUser = pingInReply }
                embed { applyPage() }

                with(this@MessageButtonPaginator.components) {
                    this@createMessage.applyToMessage()
                }
            }
        } else {
            updateButtons()

            message!!.edit {
                embed { applyPage() }

                with(this@MessageButtonPaginator.components) {
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
            message!!.delete()
        } else {
            message!!.edit {
                allowedMentions { repliedUser = pingInReply }
                embed { applyPage() }

                this.components = mutableListOf()
            }
        }

        super.destroy()
    }
}

/** Convenience function for creating a message button paginator from a paginator builder. **/
@Suppress("FunctionNaming")  // Factory function
public fun MessageButtonPaginator(
    pingInReply: Boolean = true,
    targetChannel: MessageChannelBehavior? = null,
    targetMessage: Message? = null,

    builder: PaginatorBuilder
): MessageButtonPaginator =
    MessageButtonPaginator(
        pages = builder.pages,
        owner = builder.owner,
        timeoutSeconds = builder.timeoutSeconds,
        keepEmbed = builder.keepEmbed,
        bundle = builder.bundle,
        locale = builder.locale,

        pingInReply = pingInReply,
        targetChannel = targetChannel,
        targetMessage = targetMessage,

        switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
    )
