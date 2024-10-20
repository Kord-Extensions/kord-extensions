/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.pagination

import dev.kord.core.behavior.UserBehavior
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.Message
import dev.kord.core.entity.ReactionEmoji
import dev.kord.rest.builder.message.allowedMentions
import dev.kordex.core.pagination.builders.PageTransitionCallback
import dev.kordex.core.pagination.builders.PaginatorBuilder
import dev.kordex.core.pagination.pages.Pages
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
	chunkedPages: Int = 1,
	owner: UserBehavior? = null,
	timeoutSeconds: Long? = null,
	keepEmbed: Boolean = true,
	switchEmoji: ReactionEmoji = if (pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	mutator: PageTransitionCallback? = null,
	locale: Locale? = null,

	public val pingInReply: Boolean = true,
	public val targetChannel: MessageChannelBehavior? = null,
	public val targetMessage: Message? = null,
) : BaseButtonPaginator(pages, chunkedPages, owner, timeoutSeconds, keepEmbed, switchEmoji, mutator, locale) {
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
				applyPage()

				with(this@MessageButtonPaginator.components) {
					this@createMessage.applyToMessage()
				}
			}
		} else {
			updateButtons()

			message!!.edit {
				applyPage()

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
				applyPage()

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

	builder: PaginatorBuilder,
): MessageButtonPaginator =
	MessageButtonPaginator(
		pages = builder.pages,
		chunkedPages = builder.chunkedPages,
		owner = builder.owner,
		timeoutSeconds = builder.timeoutSeconds,
		keepEmbed = builder.keepEmbed,
		mutator = builder.mutator,
		locale = builder.locale,

		pingInReply = pingInReply,
		targetChannel = targetChannel,
		targetMessage = targetMessage,

		switchEmoji = builder.switchEmoji ?: if (builder.pages.groups.size == 2) EXPAND_EMOJI else SWITCH_EMOJI,
	)
