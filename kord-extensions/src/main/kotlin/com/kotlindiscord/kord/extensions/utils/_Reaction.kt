package com.kotlindiscord.kord.extensions.utils

import dev.kord.core.entity.GuildEmoji
import dev.kord.core.entity.ReactionEmoji

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
 * Transform a [String] containing a Unicode emoji into a [ReactionEmoji].
 *
 * @receiver String containing a Unicode emoji.
 * @return Newly-created reaction emoji instance.
 *
 * @see [ReactionEmoji.Unicode]
 */
public fun String.toReaction(): ReactionEmoji = ReactionEmoji.Unicode(this)
