package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.utils.users
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MemberListConverter(
    required: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : MultiConverter<Member>(required) {
    override val signatureTypeString = "members"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val members = mutableListOf<Member>()

        for (arg in args) {
            members.add(
                findMember(arg, context, bot) ?: break
            )
        }

        parsed = members.toList()

        return parsed.size
    }

    private suspend fun findMember(arg: String, context: CommandContext, bot: ExtensibleBot): Member? {
        val user: User = if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                bot.kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                logger.debug { "Value '$id' is not a valid member ID." }

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
        } ?: return null

        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId ?: return null

        return user.asMember(guildId)
    }
}
