package com.kotlindiscord.kordex.ext.common.configuration.emoji

import com.kotlindiscord.kordex.ext.common.configuration.base.TomlConfig
import com.kotlindiscord.kordex.ext.common.configuration.emoji.spec.EmojiSpec
import dev.kord.common.entity.Snowflake

/**
 * Implementation of EmojiConfig backed by TOML files, system properties and env vars.
 *
 * For more information on how this works, see [TomlConfig] and the README.
 */
class TomlEmojiConfig : TomlConfig("emoji", EmojiSpec), EmojiConfig {
    override suspend fun getGuilds(): List<Snowflake> = config[EmojiSpec.guilds]
    override suspend fun getGuildOverride(emojiName: String): Snowflake? = config[EmojiSpec.overrides][emojiName]
}
