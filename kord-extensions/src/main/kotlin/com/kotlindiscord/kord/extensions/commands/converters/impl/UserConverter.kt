package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.User
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.utils.users
import kotlinx.coroutines.flow.firstOrNull

class UserConverter(required: Boolean = true) : SingleConverter<User>(required) {
    override val signatureTypeString = "user"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val user = findUser(arg, context, bot)
            ?: throw ParseException("Unable to find user: $arg")

        parsed = user
        return true
    }

    private suspend fun findUser(arg: String, context: CommandContext, bot: ExtensibleBot): User? =
        if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                bot.kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw ParseException("Value '$id' is not a valid emoji ID.")
            }
        } else {
            try { // Try for a user ID first
                bot.kord.getUser(Snowflake(arg))
            } catch (e: NumberFormatException) { // It's not an ID, let's try the tag
                if (!arg.contains("#")) {
                    null
                } else {
                    bot.kord.users.firstOrNull { user ->
                        user.tag.equals(arg, true)
                    }
                }
            }
        }
}

