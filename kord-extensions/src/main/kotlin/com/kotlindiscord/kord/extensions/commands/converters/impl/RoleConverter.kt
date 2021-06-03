@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Role
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.RoleBuilder
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
@OptIn(KordPreview::class)
public class RoleConverter(
    private var requiredGuild: (suspend () -> Snowflake)? = null,
    override var validator: (suspend Argument<*>.(Role) -> Unit)? = null
) : SingleConverter<Role>() {
    override val signatureTypeString: String = "converters.role.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val role = findRole(arg, context)
            ?: throw CommandException(
                context.translate("converters.role.error.missing", replacements = arrayOf(arg))
            )

        parsed = role
        return true
    }

    private suspend fun findRole(arg: String, context: CommandContext): Role? {
        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id ?: return null
        val guild = kord.getGuild(guildId) ?: return null

        @Suppress("MagicNumber")
        return if (arg.startsWith("<@&") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(3, arg.length - 1)

            try {
                guild.getRole(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate("converters.role.error.invalid", replacements = arrayOf(id))
                )
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

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        RoleBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.role(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Role) -> Unit)? = null,
): SingleConverter<Role> =
    arg(displayName, description, RoleConverter(requiredGuild, validator))

/**
 * Create an optional role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.optionalRole(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Role?) -> Unit)? = null,
): OptionalConverter<Role?> =
    arg(
        displayName,
        description,
        RoleConverter(requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a role converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RoleConverter
 */
public fun Arguments.roleList(
    displayName: String,
    description: String,
    required: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Role>) -> Unit)? = null,
): MultiConverter<Role> =
    arg(
        displayName,
        description,
        RoleConverter(requiredGuild)
            .toMulti(required, signatureTypeString = "roles", nestedValidator = validator)
    )
