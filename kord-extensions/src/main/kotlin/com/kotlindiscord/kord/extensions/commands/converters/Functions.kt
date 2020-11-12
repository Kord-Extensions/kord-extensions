package com.kotlindiscord.kord.extensions.commands.converters

import com.gitlab.kordlib.common.entity.Snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

fun Arguments.boolean(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanConverter(required))

fun Arguments.booleanList(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanListConverter(required))

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
) = arg(displayName, ChannelListConverter(required, requireSameGuild, requiredGuild))

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
    arg(displayName, DecimalListConverter(required))

fun Arguments.duration(displayName: String, required: Boolean = true) =
    arg(displayName, DurationConverter(required))

fun Arguments.durationList(displayName: String, required: Boolean = true) =
    arg(displayName, DurationListConverter(required))

fun Arguments.emoji(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiConverter(required))

fun Arguments.emojiList(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiListConverter(required))

inline fun <reified T: Enum<T>> Arguments.enum(displayName: String, typeName: String, required: Boolean = true) =
    arg(displayName, EnumConverter(required, typeName, ::getEnum, T::class))

inline fun <reified T: Enum<T>> Arguments.enumList(displayName: String, typeName: String, required: Boolean = true) =
    arg(displayName, EnumListConverter(required, typeName, ::getEnum, T::class))

fun Arguments.guild(displayName: String, required: Boolean = true) =
    arg(displayName, GuildConverter(required))

fun Arguments.guildList(displayName: String, required: Boolean = true) =
    arg(displayName, GuildListConverter(required))

fun Arguments.member(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(required, requiredGuild))

fun Arguments.memberList(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberListConverter(required, requiredGuild))

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
) = arg(displayName, MessageListConverter(required, requireGuild, requiredGuild))

fun Arguments.number(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix))

fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberListConverter(required, radix))

fun Arguments.regex(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(required, options))

fun Arguments.regexList(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexListConverter(required, options))

fun Arguments.role(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(required, requiredGuild))

fun Arguments.roleList(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleListConverter(required, requiredGuild))

fun Arguments.string(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required))

fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringListConverter(required))

fun Arguments.t4jDuration(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter(required))

fun Arguments.t4jDurationList(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationListConverter(required))

fun Arguments.user(displayName: String, required: Boolean = true) =
    arg(displayName, UserConverter(required))

fun Arguments.userList(displayName: String, required: Boolean = true) =
    arg(displayName, UserListConverter(required))

inline fun <reified T: Enum<T>> getEnum (arg: String) =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
