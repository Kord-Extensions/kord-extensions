package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.User
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.utils.users
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class UserListConverter(
    required: Boolean = true
) : MultiConverter<User>(required) {
    override val signatureTypeString = "users"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val users = mutableListOf<User>()

        for (arg in args) {
            users.add(
                findUser(arg, context, bot) ?: break
            )
        }

        parsed = users.toList()

        return parsed.size
    }

    private suspend fun findUser(arg: String, context: CommandContext, bot: ExtensibleBot): User? =
        if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                bot.kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                logger.debug { "Value '$id' is not a valid emoji ID." }

                null
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
