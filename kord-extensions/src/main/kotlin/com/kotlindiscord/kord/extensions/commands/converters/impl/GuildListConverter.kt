package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import kotlinx.coroutines.flow.firstOrNull

class GuildListConverter(required: Boolean = true) : MultiConverter<Guild>(required) {
    override val signatureTypeString = "servers"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val guilds = mutableListOf<Guild>()

        for (arg in args) {
            guilds.add(
                findGuild(arg, context, bot) ?: break
            )
        }

        parsed = guilds.toList()

        return parsed.size
    }

    private suspend fun findGuild(arg: String, context: CommandContext, bot: ExtensibleBot): Guild? =
        try { // Try for a guild ID first
            val id = Snowflake(arg)

            bot.kord.getGuild(id)
        } catch (e: NumberFormatException) { // It's not an ID, let's try the name
            bot.kord.guilds.firstOrNull { it.name.equals(arg, true) }
        }
}
