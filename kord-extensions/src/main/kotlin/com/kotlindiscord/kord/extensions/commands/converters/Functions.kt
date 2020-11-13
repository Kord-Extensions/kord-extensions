package com.kotlindiscord.kord.extensions.commands.converters

import com.gitlab.kordlib.common.entity.Snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

fun Arguments.boolean(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanConverter(required))

fun Arguments.booleanList(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanConverter(required).toMulti(errorTypeString = "multiple `yes` or `no` values"))

fun Arguments.channel(
    displayName: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, ChannelConverter(required, requireSameGuild, requiredGuild))

fun Arguments.channelList(
    displayName: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, ChannelConverter(required, requireSameGuild, requiredGuild)
    .toMulti(signatureTypeString = "channels"))

fun Arguments.coalescedDuration(displayName: String, required: Boolean = true) =
    arg(displayName, DurationCoalescingConverter(required))

fun Arguments.coalescedRegex(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexCoalescingConverter(required, options))

fun Arguments.coalescedString(displayName: String, required: Boolean = true) =
    arg(displayName, StringCoalescingConverter(required))

fun Arguments.coalescedT4jDuration(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationCoalescingConverter(required))

fun Arguments.decimal(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalConverter(required))

fun Arguments.decimalList(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalConverter(required).toMulti(signatureTypeString = "decimals"))

fun Arguments.duration(displayName: String, required: Boolean = true) =
    arg(displayName, DurationConverter(required))

fun Arguments.durationList(displayName: String, required: Boolean = true) =
    arg(displayName, DurationConverter(required).toMulti(signatureTypeString = "durations"))

fun Arguments.emoji(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiConverter(required))

fun Arguments.emojiList(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiConverter(required).toMulti(signatureTypeString = "server emojis"))

inline fun <reified T : Enum<T>> Arguments.enum(displayName: String, typeName: String, required: Boolean = true) =
    arg(displayName, EnumConverter<T>(required, typeName, ::getEnum))

inline fun <reified T : Enum<T>> Arguments.enumList(displayName: String, typeName: String, required: Boolean = true) =
    arg(displayName, EnumConverter<T>(required, typeName, ::getEnum).toMulti())

fun Arguments.guild(displayName: String, required: Boolean = true) =
    arg(displayName, GuildConverter(required))

fun Arguments.guildList(displayName: String, required: Boolean = true) =
    arg(displayName, GuildConverter(required).toMulti(signatureTypeString = "servers"))

fun Arguments.member(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(required, requiredGuild))

fun Arguments.memberList(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(required, requiredGuild).toMulti(signatureTypeString = "members"))

fun Arguments.message(
    displayName: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(required, requireGuild, requiredGuild))

fun Arguments.messageList(
    displayName: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(required, requireGuild, requiredGuild).toMulti(signatureTypeString = "messages"))

fun Arguments.number(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix))

fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix).toMulti(signatureTypeString = "numbers"))

fun Arguments.regex(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(required, options))

fun Arguments.regexList(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(required, options).toMulti(signatureTypeString = "regexes"))

fun Arguments.role(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(required, requiredGuild))

fun Arguments.roleList(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(required, requiredGuild).toMulti(signatureTypeString = "roles"))

fun Arguments.string(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required))

fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required).toMulti())

fun Arguments.t4jDuration(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter(required))

fun Arguments.t4jDurationList(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter(required).toMulti(signatureTypeString = "durations"))

fun Arguments.user(displayName: String, required: Boolean = true) =
    arg(displayName, UserConverter(required))

fun Arguments.userList(displayName: String, required: Boolean = true) =
    arg(displayName, UserConverter(required).toMulti(signatureTypeString = "users"))

inline fun <reified T : Enum<T>> getEnum(arg: String) =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
