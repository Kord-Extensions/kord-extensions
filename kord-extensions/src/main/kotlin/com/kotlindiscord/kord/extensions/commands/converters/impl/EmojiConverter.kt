package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.GuildEmoji
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull

class EmojiConverter(required: Boolean = true) : SingleConverter<GuildEmoji>(required) {
    override val signatureTypeString = "server emoji"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val emoji = findEmoji(arg, context, bot)
            ?: throw ParseException("Unable to find emoji: $arg")

        parsed = emoji
        return true
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
                throw ParseException("Value '$id' is not a valid emoji ID.")
            }
        } else { // ID or name
            try {
                val snowflake = Snowflake(arg)

                bot.kord.guilds.mapNotNull {
                    it.getEmojiOrNull(snowflake)
                }.firstOrNull()
            } catch (e: NumberFormatException) {  // Not an ID, let's check names
                bot.kord.guilds.mapNotNull {
                    it.emojis.first { emojiObj -> emojiObj.name?.toLowerCase().equals(arg, true) }
                }.firstOrNull()
            }
        }
}
