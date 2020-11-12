package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Role
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import kotlinx.coroutines.flow.firstOrNull

class RoleConverter(
    required: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : SingleConverter<Role>(required) {
    override val signatureTypeString = "role"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val role = findRole(arg, context, bot)
            ?: throw ParseException("Unable to find role: $arg")

        parsed = role
        return true
    }

    private suspend fun findRole(arg: String, context: CommandContext, bot: ExtensibleBot): Role? {
        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId ?: return null
        val guild = bot.kord.getGuild(guildId) ?: return null

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
