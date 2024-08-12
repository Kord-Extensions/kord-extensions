/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.types

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji

/**
 * Interface representing a button type that has a partial emoji property. This is used to keep the [emoji]
 * function DRY.
 */
public interface HasPartialEmoji {
	/**
	 * A partial emoji object, either a guild or Unicode emoji. Optional if you've got a label.
	 *
	 * @see emoji
	 */
	public var partialEmoji: DiscordPartialEmoji?
}

/** Convenience function for setting [HasPartialEmoji.partialEmoji] based on a given Unicode emoji. **/
public fun HasPartialEmoji.emoji(unicodeEmoji: String) {
	partialEmoji = DiscordPartialEmoji(
		name = unicodeEmoji
	)
}

/** Convenience function for setting [HasPartialEmoji.partialEmoji] based on a given guild custom emoji. **/
public fun HasPartialEmoji.emoji(guildEmoji: GuildEmoji) {
	partialEmoji = DiscordPartialEmoji(
		id = guildEmoji.id,
		name = guildEmoji.name,
		animated = guildEmoji.isAnimated.optional()
	)
}

/** Convenience function for setting [HasPartialEmoji.partialEmoji] based on a given reaction emoji. **/
public fun HasPartialEmoji.emoji(unicodeEmoji: ReactionEmoji.Unicode) {
	partialEmoji = DiscordPartialEmoji(
		name = unicodeEmoji.name
	)
}

/** Convenience function for setting [HasPartialEmoji.partialEmoji] based on a given reaction emoji. **/
public fun HasPartialEmoji.emoji(guildEmoji: ReactionEmoji.Custom) {
	partialEmoji = DiscordPartialEmoji(
		id = guildEmoji.id,
		name = guildEmoji.name,
		animated = guildEmoji.isAnimated.optional()
	)
}

/** Convenience function for setting [HasPartialEmoji.partialEmoji] based on a given reaction emoji. **/
public fun HasPartialEmoji.emoji(emoji: ReactionEmoji): Unit = when (emoji) {
	is ReactionEmoji.Unicode -> emoji(emoji)
	is ReactionEmoji.Custom -> emoji(emoji)
}
