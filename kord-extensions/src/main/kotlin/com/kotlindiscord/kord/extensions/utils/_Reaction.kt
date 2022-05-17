/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

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
 * Unicode emoji. Custom emoji must match one of the following formats:
 *
 * * Animated emoji: `<a:name:id>`
 * * Normal emoji: `<:name:id>`
 *
 * @receiver String containing a Unicode emoji.
 * @return Newly-created reaction emoji instance.
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
