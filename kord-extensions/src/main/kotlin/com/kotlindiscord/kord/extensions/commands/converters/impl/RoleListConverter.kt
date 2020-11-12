package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Role
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import kotlinx.coroutines.flow.firstOrNull
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class RoleListConverter(
    required: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : MultiConverter<Role>(required) {
    override val signatureTypeString = "roles"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val roles = mutableListOf<Role>()

        for (arg in args) {
            roles.add(
                findRole(arg, context, bot) ?: break
            )
        }

        parsed = roles.toList()

        return parsed.size
    }

    private suspend fun findRole(arg: String, context: CommandContext, bot: ExtensibleBot): Role? {
        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId ?: return null
        val guild = bot.kord.getGuild(guildId) ?: return null

        return if (arg.startsWith("<@&") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(3, arg.length - 1)

            try {
                guild.getRole(Snowflake(id))
            } catch (e: NumberFormatException) {
                logger.debug { "Value '$id' is not a valid emoji ID." }

                null
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
