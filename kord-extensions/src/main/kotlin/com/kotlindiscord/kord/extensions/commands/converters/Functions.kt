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
    requiredGuild: Snowflake? = null
) = arg(displayName, ChannelConverter(required, requireSameGuild, requiredGuild))

fun Arguments.channelList(
    displayName: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: Snowflake? = null
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

fun Arguments.number(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix))

fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberListConverter(required, radix))

fun Arguments.regex(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(required, options))

fun Arguments.regexList(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexListConverter(required, options))

fun Arguments.string(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required))

fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringListConverter(required))

fun Arguments.t4jDuration(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter(required))

fun Arguments.t4jDurationList(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationListConverter(required))
