package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import kotlinx.coroutines.flow.firstOrNull

class GuildConverter(required: Boolean = true) : SingleConverter<Guild>(required) {
    override val signatureTypeString = "server"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val guild = findGuild(arg, context, bot)
            ?: throw ParseException("Unable to find server: $arg")

        parsed = guild
        return true
    }

    private suspend fun findGuild(arg: String, context: CommandContext, bot: ExtensibleBot): Guild? =
        try { // Try for a guild ID first
            val id = Snowflake(arg)

            bot.kord.getGuild(id)
        } catch (e: NumberFormatException) { // It's not an ID, let's try the name
            bot.kord.guilds.firstOrNull { it.name.equals(arg, true) }
        }
}
