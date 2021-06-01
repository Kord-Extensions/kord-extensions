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
    override var validator: (suspend Argument<*>.(Member) -> Unit)? = null
) : SingleConverter<Member>() {
    override val signatureTypeString: String = "converters.member.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val member = findMember(arg, context)
            ?: throw CommandException(
                context.translate("converters.member.error.missing", replacements = arrayOf(arg))
            )

        parsed = member
        return true
    }

    private suspend fun findMember(arg: String, context: CommandContext): Member? {
        val user: User? = if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate("converters.member.error.invalid", replacements = arrayOf(id))
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

        val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id ?: return null

        return user?.asMember(guildId)
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        UserBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
public fun Arguments.member(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Member) -> Unit)? = null,
): SingleConverter<Member> =
    arg(displayName, description, MemberConverter(requiredGuild, validator))

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
public fun Arguments.optionalMember(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Member?) -> Unit)? = null,
): OptionalConverter<Member?> =
    arg(
        displayName,
        description,
        MemberConverter(requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a member converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MemberConverter
 */
public fun Arguments.memberList(
    displayName: String,
    description: String,
    required: Boolean,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Member>) -> Unit)? = null,
): MultiConverter<Member> =
    arg(
        displayName,
        description,
        MemberConverter(requiredGuild)
            .toMulti(required, signatureTypeString = "members", nestedValidator = validator)
    )
