package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.GuildEmoji
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class EmojiListConverter(required: Boolean = true) : MultiConverter<GuildEmoji>(required) {
    override val signatureTypeString = "server emojis"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val emojis = mutableListOf<GuildEmoji>()

        for (arg in args) {
            emojis.add(
                findEmoji(arg, context, bot) ?: break
            )
        }

        parsed = emojis.toList()

        return parsed.size
    }

    private suspend fun findEmoji(arg: String, context: CommandContext, bot: ExtensibleBot): GuildEmoji? =
        if (arg.startsWith("<a:") || arg.startsWith("<:") && arg.endsWith('>')) { // Emoji mention
            val id = arg.substring(0, arg.length - 1).split(":").last()

            try {
                val snowflake = Snowflake(id)

                bot.kord.guilds.mapNotNull {
                    it.getEmojiOrNull(snowflake)
                }.firstOrNull()
            } catch (e: NumberFormatException) {
                logger.debug { "Value '$id' is not a valid emoji ID." }

                null
            }
        } else { // ID or name
            var name = arg

            if (name.startsWith(":") && name.endsWith(":")) {
                name = name.substring(1, name.length - 1)
            }

            try {
                val snowflake = Snowflake(name)

                bot.kord.guilds.mapNotNull {
                    it.getEmojiOrNull(snowflake)
                }.firstOrNull()
            } catch (e: NumberFormatException) {  // Not an ID, let's check names
                bot.kord.guilds.mapNotNull {
                    it.emojis.first { emojiObj -> emojiObj.name?.toLowerCase().equals(name, true) }
                }.firstOrNull()
            }
        }
}
