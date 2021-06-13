package com.kotlindiscord.kordex.ext.common.extensions

import com.kotlindiscord.kord.extensions.checks.inGuild
import com.kotlindiscord.kord.extensions.checks.or
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kordex.ext.common.builders.ExtCommonBuilder
import com.kotlindiscord.kordex.ext.common.configuration.emoji.EmojiConfig
import com.kotlindiscord.kordex.ext.common.emoji.NamedEmoji
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.GuildEmoji
import dev.kord.core.event.guild.EmojisUpdateEvent
import dev.kord.core.event.guild.GuildCreateEvent
import kotlinx.coroutines.flow.toList

/**
 * Emoji extension, in charge of keeping track of custom emoji so you can easily retrieve them later.
 */
class EmojiExtension : Extension() {
    override val name: String = "emoji"

    override suspend fun setup() {
        event<GuildCreateEvent> {
            action { populateEmojis() }
        }

        event<EmojisUpdateEvent> {
            check(
                or(
                    // No configured guilds? Do them all.
                    { config.getGuilds().isEmpty() },

                    {
                        config.getGuilds()
                            .mapNotNull { kord.getGuild(it) }
                            .map { inGuild { it } }
                            .any()
                    }
                )
            )

            action { populateEmojis(event.guildId) }
        }
    }

    private suspend fun populateEmojis(forGuildId: Snowflake? = null) {
        val configuredGuilds = config.getGuilds()

        val emojiGuilds = if (configuredGuilds.isEmpty()) {
            // No configured guilds? Do them all. Sorted by join time.

            kord.guilds.toList().sortedBy { it.joinedTime }
        } else {
            configuredGuilds.mapNotNull { kord.getGuild(it) }
        }

        val guildEmojis = emojiGuilds.map { guild ->
            guild.id to guild.emojis.toList()
        }.toMap()

        if (forGuildId != null) {
            emojis.entries.removeAll { (_, emoji) -> emoji.guildId != forGuildId }
        } else {
            emojis.clear()
        }

        emojiGuilds.map { it.id }.forEach { guildId ->
            if (forGuildId == null || forGuildId == guildId) {
                guildEmojis[guildId]!!.forEach emojiLoop@{
                    if (it.name == null) {
                        return@emojiLoop
                    }

                    val override = config.getGuildOverride(it.name!!)

                    if (override != null && override != guildId) {
                        return@emojiLoop
                    }

                    if (override != null || !emojis.containsKey(it.name)) {
                        // We do the check this way in case we had an older emoji cached or something
                        emojis[it.name!!] = it
                    }
                }
            }
        }
    }

    companion object {
        private val emojis: MutableMap<String, GuildEmoji> = mutableMapOf()
        private var builder: ExtCommonBuilder = ExtCommonBuilder()
        private var wasConfigured: Boolean = false

        private val config: EmojiConfig get() = builder.emojiConfig

        /**
         * Get an emoji mention by string name, using the default parameter if it can't be found instead.
         *
         * @param name Emoji to retrieve.
         * @param default String value to use if the emoji can't be found, defaulting to `:name:`.
         */
        fun getEmoji(name: String, default: String = ":$name:"): String =
            emojis[name]?.mention ?: default

        /**
         * Get an emoji mention by [NamedEmoji] value, using the default property if the emoji can't be found instead.
         *
         * @param emoji Emoji to retrieve.
         */
        fun getEmoji(emoji: NamedEmoji): String =
            emojis[emoji.name]?.mention ?: emoji.default

        /** @suppress Internal function used to pass the configured builder into the extension. **/
        fun configure(builder: ExtCommonBuilder) {
            this.builder = builder

            wasConfigured = true
        }

        /** Returns `true` if the extension has been configured, `false` if it's using the default. **/
        fun isConfigured() = wasConfigured
    }
}
