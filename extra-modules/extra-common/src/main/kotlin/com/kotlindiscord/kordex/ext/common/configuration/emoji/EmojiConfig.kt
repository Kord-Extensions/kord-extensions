/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kordex.ext.common.configuration.emoji

import com.kotlindiscord.kordex.ext.common.emoji.NamedEmoji
import dev.kord.common.entity.Snowflake

/**
 * Simple config adapter interface, which you can implement yourself if you need some kind of alternative config
 * backend.
 */
interface EmojiConfig {
    /** Get a list of guild IDs to retrieve emoji from, with earlier guilds taking priority over later guilds. **/
    suspend fun getGuilds(): List<Snowflake>

    /**
     * Get the snowflake for the that a given emoji should always be retrieved from (if any), regardless of priority.
     */
    suspend fun getGuildOverride(emojiName: String): Snowflake?

    /**
     * Get the snowflake for the that a given emoji should always be retrieved from (if any), regardless of priority.
     */
    suspend fun getGuildOverride(emoji: NamedEmoji): Snowflake? = getGuildOverride(emoji.name)
}
