/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.utils

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji

internal val CUSTOM_EMOJI_REGEX = "<(a)?:([^:]+):(\\d+)>".toRegex()

/**
 * Transform the given [GuildEmoji] into a [ReactionEmoji].
 *
 * @receiver Guild emoji instance.
 * @return Newly-created reaction emoji instance.
 *
 * @see [ReactionEmoji.from]
 */
public fun GuildEmoji.toReaction(): ReactionEmoji = ReactionEmoji.from(this)

/**
 * Transform a [String] containing an emoji into a [ReactionEmoji].
 *
 * This will attempt to parse the string as a custom emoji first and, if it can't, it'll assume you've given it a
 * Unicode emoji.
 * **Note:** This may result in an invalid [ReactionEmoji], so you may prefer to use the jemoji EmojiManager to check.
 *
 * All custom emoji must match one of the following formats:
 *
 * * Animated emoji: `<a:name:id>`
 * * Normal emoji: `<:name:id>`
 *
 * @receiver String containing a Unicode or custom emoji.
 * @return Newly created reaction emoji instance.
 *
 * @see [ReactionEmoji.Unicode]
 */
@Suppress("MagicNumber")
public fun String.toReaction(): ReactionEmoji {
	val match = CUSTOM_EMOJI_REGEX.matchEntire(this)
		?: return ReactionEmoji.Unicode(this)

	val groups = match.groupValues
	val isAnimated = groups[1].isNotEmpty()

	return ReactionEmoji.Custom(Snowflake(groups[3]), groups[2], isAnimated)
}

/**
 * Wrapper function for the [String.toReaction] function.
 */
public fun ReactionEmoji.Companion.from(emoji: String): ReactionEmoji =
	emoji.toReaction()
