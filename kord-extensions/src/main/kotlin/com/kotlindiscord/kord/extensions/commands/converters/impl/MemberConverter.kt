package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.member
import com.kotlindiscord.kord.extensions.commands.converters.memberList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.utils.users
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.UserBuilder
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
@OptIn(KordPreview::class)
public class MemberConverter(
    private var requiredGuild: (suspend () -> Snowflake)? = null,
    override var validator: (suspend (Member) -> Unit)? = null
) : SingleConverter<Member>() {
    override val signatureTypeString: String = "member"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val member = findMember(arg, context, bot)
            ?: throw CommandException("Unable to find member: $arg")

        parsed = member
        return true
    }

    private suspend fun findMember(arg: String, context: CommandContext, bot: ExtensibleBot): Member? {
        val user: User? = if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                bot.kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw CommandException("Value '$id' is not a valid member ID.")
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

        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id ?: return null

        return user?.asMember(guildId)
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        UserBuilder(arg.displayName, arg.description).apply { required = true }
}
