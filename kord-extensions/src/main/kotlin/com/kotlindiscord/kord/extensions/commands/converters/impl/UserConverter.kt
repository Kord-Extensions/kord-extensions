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
import com.kotlindiscord.kord.extensions.utils.users
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.UserBuilder
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for discord [User] arguments.
 *
 * This converter supports specifying members by supplying:
 * * A user or member mention
 * * A user ID
 * * The user's tag (`username#discriminator`)
 *
 * @see user
 * @see userList
 */
@OptIn(KordPreview::class)
public class UserConverter(
    override var validator: (suspend Argument<*>.(User) -> Unit)? = null
) : SingleConverter<User>() {
    override val signatureTypeString: String = "converters.user.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val user = findUser(arg, context)
            ?: throw CommandException(
                context.translate("converters.user.error.missing", replacements = arrayOf(arg))
            )

        parsed = user
        return true
    }

    private suspend fun findUser(arg: String, context: CommandContext): User? =
        if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate("converters.user.error.invalid", replacements = arrayOf(id))
                )
            }
        } else {
            try { // Try for a user ID first
                kord.getUser(Snowflake(arg))
            } catch (e: NumberFormatException) { // It's not an ID, let's try the tag
                if (!arg.contains("#")) {
                    null
                } else {
                    kord.users.firstOrNull { user ->
                        user.tag.equals(arg, true)
                    }
                }
            }
        }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        UserBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a user converter, for single arguments.
 *
 * @see UserConverter
 */
public fun Arguments.user(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(User) -> Unit)? = null,
): SingleConverter<User> =
    arg(displayName, description, UserConverter(validator))

/**
 * Create an optional user converter, for single arguments.
 *
 * @see UserConverter
 */
public fun Arguments.optionalUser(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(User?) -> Unit)? = null,
): OptionalConverter<User?> =
    arg(
        displayName,
        description,
        UserConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a user converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see UserConverter
 */
public fun Arguments.userList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<User>) -> Unit)? = null,
): MultiConverter<User> =
    arg(
        displayName,
        description,
        UserConverter()
            .toMulti(required, signatureTypeString = "users", nestedValidator = validator)
    )
