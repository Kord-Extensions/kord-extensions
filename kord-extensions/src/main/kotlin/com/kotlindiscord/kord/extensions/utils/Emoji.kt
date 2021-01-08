@file:JvmMultifileClass
@file:JvmName("EmojiKt")

package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji

/**
 * Transform the [GuildEmoji] to a [ReactionEmoji]
 * @receiver Instance of a Discord Emoji 
 * @return A new instance of [ReactionEmoji]
 * @see [ReactionEmoji.from]
 */
public fun GuildEmoji.toReaction() : ReactionEmoji
    = ReactionEmoji.from(this)

/**
 * Transform a [String] with the unicode format to a [ReactionEmoji]
 * @receiver String with unicode format
 * @return A new instance of [ReactionEmoji]
 * @see [ReactionEmoji.Unicode]
 */
public fun String.toReaction(): ReactionEmoji
    = ReactionEmoji.Unicode(this)
