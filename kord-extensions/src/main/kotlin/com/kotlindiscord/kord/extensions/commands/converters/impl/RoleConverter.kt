package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Role
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.role
import com.kotlindiscord.kord.extensions.commands.converters.roleList
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for discord [Role] arguments.
 *
 * This converter supports specifying roles by supplying:
 * * A role mention
 * * A role ID
 * * A message name - the first matching role from the given guild will be used.
 *
 * @param requiredGuild Lambda returning a specific guild to require the role to be in. If omitted, defaults to the
 * guild the command was invoked in.
 *
 * @see role
 * @see roleList
 */
public class RoleConverter(
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : SingleConverter<Role>() {
    override val signatureTypeString: String = "role"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val role = findRole(arg, context, bot)
            ?: throw ParseException("Unable to find role: $arg")

        parsed = role
        return true
    }

    private suspend fun findRole(arg: String, context: CommandContext, bot: ExtensibleBot): Role? {
        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId ?: return null
        val guild = bot.kord.getGuild(guildId) ?: return null

        @Suppress("MagicNumber")
        return if (arg.startsWith("<@&") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(3, arg.length - 1)

            try {
                guild.getRole(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw ParseException("Value '$id' is not a valid emoji ID.")
            }
        } else {
            try { // Try for a role ID first
                guild.getRole(Snowflake(arg))
            } catch (e: NumberFormatException) { // It's not an ID, let's try the name
                guild.roles.firstOrNull { role ->
                    role.name.equals(arg, true)
                }
            }
        }
    }
}
