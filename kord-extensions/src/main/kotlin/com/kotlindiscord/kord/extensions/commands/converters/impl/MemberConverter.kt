/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.chat.ChatCommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.users
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.MemberOptionValue
import dev.kord.core.entity.interaction.OptionValue
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
 * * "me" to refer to the member running the command
 *
 * @param requiredGuild Lambda returning a specific guild to require the member to be in, if needed.
 * @param useReply Whether to use the author of the replied-to message (if there is one) instead of trying to parse an
 * argument.
 */
@Converter(
    "member",

    types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
    imports = ["dev.kord.common.entity.Snowflake"],

    builderFields = [
        "public var requiredGuild: (suspend () -> Snowflake)? = null",
        "public var useReply: Boolean = true",
        "public var requireSameGuild: Boolean = true",
    ]
)
public class MemberConverter(
    private var requiredGuild: (suspend () -> Snowflake)? = null,
    private var useReply: Boolean = true,
    private var requireSameGuild: Boolean = true,
    override var validator: Validator<Member> = null,
) : SingleConverter<Member>() {
    override val signatureTypeString: String = "converters.member.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val guild = context.getGuild()

        if (requireSameGuild && requiredGuild == null && guild != null) {
            requiredGuild = { guild.id }
        }

        if (useReply && context is ChatCommandContext<*>) {
            val messageReference = context.message.asMessage().messageReference

            if (messageReference != null) {
                val member = messageReference.message?.asMessage()?.getAuthorAsMember()

                if (member != null) {
                    parsed = member
                    return true
                }
            }
        }

        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        if (arg.equals("me", true)) {
            val member = context.getMember()?.asMemberOrNull()

            if (member != null) {
                this.parsed = member

                return true
            }
        }

        parsed = findMember(arg, context)
            ?: throw DiscordRelayedException(
                context.translate("converters.member.error.missing", replacements = arrayOf(arg))
            )

        return true
    }

    private suspend fun findMember(arg: String, context: CommandContext): Member? {
        val user: User? = if (arg.startsWith("<@") && arg.endsWith(">")) { // It's a mention
            val id: String = arg.substring(2, arg.length - 1).replace("!", "")

            try {
                kord.getUser(Snowflake(id))
            } catch (e: NumberFormatException) {
                throw DiscordRelayedException(
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

        val currentGuild = context.getGuild()

        val guildId: Snowflake? = if (requiredGuild != null) {
            requiredGuild!!.invoke()
        } else {
            currentGuild?.id
        }

        if (guildId != currentGuild?.id) {
            throw DiscordRelayedException(
                context.translate("converters.member.error.invalid", replacements = arrayOf(user?.tag ?: arg))
            )
        }

        return user?.asMember(
            guildId ?: return null
        )
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        UserBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? MemberOptionValue)?.resolvedObject ?: return false
        val guild = context.getGuild()

        if (requireSameGuild && requiredGuild == null && guild != null) {
            requiredGuild = { guild.id }
        }

        val requiredGuildId = requiredGuild?.invoke()

        if (requiredGuildId != null && optionValue.guildId != requiredGuildId) {
            throw DiscordRelayedException(
                context.translate("converters.member.error.invalid", replacements = arrayOf(optionValue.tag))
            )
        }

        this.parsed = optionValue

        return true
    }
}
