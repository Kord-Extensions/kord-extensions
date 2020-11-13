package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Member
import com.gitlab.kordlib.core.entity.User
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.member
import com.kotlindiscord.kord.extensions.commands.converters.memberList
import com.kotlindiscord.kord.extensions.utils.users
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for discord [Member] arguments.
 *
 * Members represent Discord users that are part of a guild. This converter supports specifying members by supplying:
 * * A user or member mention
 * * A user ID
 * * The user's tag (`username#discriminator`)
 *
 * @param requiredGuild Lambda returning a specific guild to require the member to be in, if needed.
 *
 * @see member
 * @see memberList
 */
class MemberConverter(
    required: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : SingleConverter<Member>(required) {
    override val signatureTypeString = "member"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val member = findMember(arg, context, bot)
            ?: throw ParseException("Unable to find member: $arg")

        parsed = member
        return true
    }

    private suspend fun findMember(arg: String, context: CommandContext, bot: ExtensibleBot): Member? {
        val user: User? = if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                bot.kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw ParseException("Value '$id' is not a valid member ID.")
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

        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId ?: return null

        return user?.asMember(guildId)
    }
}
