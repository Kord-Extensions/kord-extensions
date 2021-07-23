@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.common.entity.optional.optional
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji
import org.koin.core.component.KoinComponent

/**
 * Abstract class representing a button builder, providing common functionality and properties.
 */
public interface ButtonBuilder : KoinComponent {
    /**
     * The button's label text. Optional if you've got an emoji.
     *
     * Labels default to a zero-width space. This does make them slightly wider than usual if you don't set your
     * own label, but it means that iOS users can tap emoji-only buttons without having to specifically tap the
     * emoji.
     */
    public var label: String?

    /**
     * A partial emoji object, either a guild or Unicode emoji. Optional if you've got a label.
     *
     * @see emoji
     */
    public var partialEmoji: DiscordPartialEmoji?

    /** Convenience function for setting [partialEmoji] based on a given Unicode emoji. **/
    public fun emoji(unicodeEmoji: String) {
        partialEmoji = DiscordPartialEmoji(
            name = unicodeEmoji
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given guild custom emoji. **/
    public fun emoji(guildEmoji: GuildEmoji) {
        partialEmoji = DiscordPartialEmoji(
            id = guildEmoji.id,
            name = guildEmoji.name,
            animated = guildEmoji.isAnimated.optional()
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given reaction emoji. **/
    public fun emoji(unicodeEmoji: ReactionEmoji.Unicode) {
        partialEmoji = DiscordPartialEmoji(
            name = unicodeEmoji.name
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given reaction emoji. **/
    public fun emoji(guildEmoji: ReactionEmoji.Custom) {
        partialEmoji = DiscordPartialEmoji(
            id = guildEmoji.id,
            name = guildEmoji.name,
            animated = guildEmoji.isAnimated.optional()
        )
    }

    /** Convenience function for setting [partialEmoji] based on a given reaction emoji. **/
    public fun emoji(emoji: ReactionEmoji): Unit = when (emoji) {
        is ReactionEmoji.Unicode -> emoji(emoji)
        is ReactionEmoji.Custom -> emoji(emoji)
    }
}
