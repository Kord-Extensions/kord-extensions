/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils.deltas

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.core.entity.*
import kotlinx.datetime.Instant
import kotlin.contracts.contract

/**
 * Represents the difference between two Kord [Message] objects.
 *
 * This is intended for use with events that change things, to make logging easier - but may have other applications.
 * All properties available on this object have the same names as the corresponding properties on the [Message] object.
 *
 * Optionals will be [Optional.Missing] if there was no change - otherwise they'll contain the value from the `new`
 * [Message].
 */
@Suppress("UndocumentedPublicProperty")
public open class MessageDelta(
	public val attachments: Optional<Set<Attachment>>,
	public val content: Optional<String>,
	public val editedTimestamp: Optional<Instant?>,
	public val embeds: Optional<List<Embed>>,
	public val isPinned: Optional<Boolean>,
	public val mentionedChannelIds: Optional<Set<Snowflake>>,
	public val mentionedRoleIds: Optional<Set<Snowflake>>,
	public val mentionedUserIds: Optional<Set<Snowflake>>,
	public val mentionsEveryone: Optional<Boolean>,
	public val reactions: Optional<Set<Reaction>>,
	public val stickers: Optional<List<StickerItem>>,
) {
	/**
	 * A Set representing the values that have changes. Each value is represented by a human-readable string.
	 */
	public open val changes: Set<String> by lazy {
		mutableSetOf<String>().apply {
			if (attachments !is Optional.Missing) add("attachments")
			if (content !is Optional.Missing) add("content")
			if (editedTimestamp !is Optional.Missing) add("editedTimestamp")
			if (embeds !is Optional.Missing) add("embeds")
			if (isPinned !is Optional.Missing) add("isPinned")
			if (mentionedChannelIds !is Optional.Missing) add("mentionedChannelIds")
			if (mentionedRoleIds !is Optional.Missing) add("mentionedRoleIds")
			if (mentionedUserIds !is Optional.Missing) add("mentionedUserIds")
			if (mentionsEveryone !is Optional.Missing) add("mentionsEveryone")
			if (reactions !is Optional.Missing) add("reactions")
			if (stickers !is Optional.Missing) add("stickers")
		}
	}

	public companion object {
		/**
		 * Given an old and new [User] object, return a [MessageDelta] representing the changes between them.
		 *
		 * @param old The older [User] object.
		 * @param new The newer [User] object.
		 */
		public fun from(old: Message?, new: Message): MessageDelta? {
			contract {
				returns(null) implies (old == null)
			}

			old ?: return null

			return MessageDelta(
				if (old.attachments != new.attachments) Optional(new.attachments) else Optional.Missing(),
				if (old.content != new.content) Optional(new.content) else Optional.Missing(),
				if (old.editedTimestamp != new.editedTimestamp) Optional(new.editedTimestamp) else Optional.Missing(),
				if (old.embeds != new.embeds) Optional(new.embeds) else Optional.Missing(),
				if (old.isPinned != new.isPinned) Optional(new.isPinned) else Optional.Missing(),

				if (old.mentionedChannelIds != new.mentionedChannelIds) {
					Optional(new.mentionedChannelIds)
				} else {
					Optional.Missing()
				},

				if (old.mentionedRoleIds != new.mentionedRoleIds) {
					Optional(new.mentionedRoleIds)
				} else {
					Optional.Missing()
				},

				if (old.mentionedUserIds != new.mentionedUserIds) {
					Optional(new.mentionedUserIds)
				} else {
					Optional.Missing()
				},

				if (old.mentionsEveryone != new.mentionsEveryone) {
					Optional(new.mentionsEveryone)
				} else {
					Optional.Missing()
				},

				if (old.reactions != new.reactions) Optional(new.reactions) else Optional.Missing(),
				if (old.stickers != new.stickers) Optional(new.stickers) else Optional.Missing(),
			)
		}
	}
}
