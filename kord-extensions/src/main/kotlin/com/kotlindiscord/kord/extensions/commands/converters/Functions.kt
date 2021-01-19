@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
import net.time4j.IsoUnit
import java.time.Duration

// region: Required (single) converters

/**
 * Create a boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.boolean(displayName: String, description: String): SingleConverter<Boolean> =
    arg(displayName, description, BooleanConverter())

/**
 * Create a channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.channel(
    displayName: String,
    description: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
): SingleConverter<Channel> = arg(displayName, description, ChannelConverter(requireSameGuild, requiredGuild))

/**
 * Create a decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.decimal(displayName: String, description: String): SingleConverter<Double> =
    arg(displayName, description, DecimalConverter())

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.duration(displayName: String, description: String): SingleConverter<Duration> =
    arg(displayName, description, DurationConverter())

/**
 * Create an email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.email(displayName: String, description: String): SingleConverter<String> =
    arg(displayName, description, EmailConverter())

/**
 * Create an emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.emoji(displayName: String, description: String): SingleConverter<GuildEmoji> =
    arg(displayName, description, EmojiConverter())

/**
 * Create a guild converter, for single arguments.
 *
 * @see GuildConverter
 */
public fun Arguments.guild(displayName: String, description: String): SingleConverter<Guild> =
    arg(displayName, description, GuildConverter())

/**
 * Create an integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.int(displayName: String, description: String, radix: Int = 10): SingleConverter<Int> =
    arg(displayName, description, IntConverter(radix))

/**
 * Create a long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.long(displayName: String, description: String, radix: Int = 10): SingleConverter<Long> =
    arg(displayName, description, LongConverter(radix))

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
public fun Arguments.member(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)?
): SingleConverter<Member> =
    arg(displayName, description, MemberConverter(requiredGuild))

/**
 * Create a message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.message(
    displayName: String,
    description: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
): SingleConverter<Message> = arg(displayName, description, MessageConverter(requireGuild, requiredGuild))

/**
 * Create a whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
@Deprecated(
    "Renamed to long",
    replaceWith = ReplaceWith(
        "long(displayName, description, radix)",
        "com.kotlindiscord.kord.extensions.commands.converters"
    ),
    level = DeprecationLevel.WARNING
)
public fun Arguments.number(displayName: String, description: String, radix: Int = 10): SingleConverter<Long> =
    arg(displayName, description, NumberConverter(radix))

/**
 * Create a regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.regex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf()
): SingleConverter<Regex> =
    arg(displayName, description, RegexConverter(options))

/**
 * Create a role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.role(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)?
): SingleConverter<Role> =
    arg(displayName, description, RoleConverter(requiredGuild))

/**
 * Create a string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.string(displayName: String, description: String): SingleConverter<String> =
    arg(displayName, description, StringConverter())

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDuration(
    displayName: String,
    description: String
): SingleConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationConverter())

/**
 * Create a user converter, for single arguments.
 *
 * @see UserConverter
 */
public fun Arguments.user(displayName: String, description: String): SingleConverter<User> =
    arg(displayName, description, UserConverter())

// endregion

// region: Optional converters

/**
 * Create an optional boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.optionalBoolean(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<Boolean?> =
    arg(displayName, description, BooleanConverter().toOptional(outputError = outputError))

/**
 * Create an optional channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.optionalChannel(
    displayName: String,
    description: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false
): OptionalConverter<Channel?> = arg(
    displayName,
    description,
    ChannelConverter(requireSameGuild, requiredGuild)
        .toOptional(outputError = outputError)
)

/**
 * Create an optional decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.optionalDecimal(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<Double?> =
    arg(displayName, description, DecimalConverter().toOptional(outputError = outputError))

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.optionalDuration(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<Duration?> =
    arg(displayName, description, DurationConverter().toOptional(outputError = outputError))

/**
 * Create an optional email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.optionalEmail(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<String?> =
    arg(displayName, description, EmailConverter().toOptional(outputError = outputError))

/**
 * Create an optional emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.optionalEmoji(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<GuildEmoji?> =
    arg(displayName, description, EmojiConverter().toOptional(outputError = outputError))

/**
 * Create an optional guild converter, for single arguments.
 *
 * @see GuildConverter
 */
public fun Arguments.optionalGuild(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<Guild?> =
    arg(displayName, description, GuildConverter().toOptional(outputError = outputError))

/**
 * Create an optional integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.optionalInt(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10
): OptionalConverter<Int?> =
    arg(displayName, description, IntConverter(radix).toOptional(outputError = outputError))

/**
 * Create an optional long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.optionalLong(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10
): OptionalConverter<Long?> =
    arg(displayName, description, LongConverter(radix).toOptional(outputError = outputError))

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
public fun Arguments.optionalMember(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)?,
    outputError: Boolean = false
): OptionalConverter<Member?> =
    arg(displayName, description, MemberConverter(requiredGuild).toOptional(outputError = outputError))

/**
 * Create an optional message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.optionalMessage(
    displayName: String,
    description: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false
): OptionalConverter<Message?> = arg(
    displayName,
    description,
    MessageConverter(requireGuild, requiredGuild)
        .toOptional(outputError = outputError)
)

/**
 * Create an optional whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
@Deprecated(
    "Renamed to optionalLong",
    replaceWith = ReplaceWith(
        "optionalLong(displayName, description, outputError, radix)",
        "com.kotlindiscord.kord.extensions.commands.converters"
    ),
    level = DeprecationLevel.WARNING
)
public fun Arguments.optionalNumber(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10
): OptionalConverter<Long?> =
    arg(displayName, description, NumberConverter(radix).toOptional(outputError = outputError))

/**
 * Create an optional regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.optionalRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    outputError: Boolean = false
): OptionalConverter<Regex?> =
    arg(displayName, description, RegexConverter(options).toOptional(outputError = outputError))

/**
 * Create an optional role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.optionalRole(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)?,
    outputError: Boolean = false
): OptionalConverter<Role?> =
    arg(displayName, description, RoleConverter(requiredGuild).toOptional(outputError = outputError))

/**
 * Create an optional string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.optionalString(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<String?> =
    arg(displayName, description, StringConverter().toOptional(outputError = outputError))

/**
 * Create an optional Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.optionalT4jDuration(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<net.time4j.Duration<IsoUnit>?> =
    arg(displayName, description, T4JDurationConverter().toOptional(outputError = outputError))

/**
 * Create an optional user converter, for single arguments.
 *
 * @see UserConverter
 */
public fun Arguments.optionalUser(
    displayName: String,
    description: String,
    outputError: Boolean = false
): OptionalConverter<User?> =
    arg(displayName, description, UserConverter().toOptional(outputError = outputError))

// endregion

// region: Defaulting converters

/**
 * Create a defaulting boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.defaultingBoolean(
    displayName: String,
    description: String,
    defaultValue: Boolean
): DefaultingConverter<Boolean> =
    arg(displayName, description, BooleanConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.defaultingDecimal(
    displayName: String,
    description: String,
    defaultValue: Double
): DefaultingConverter<Double> =
    arg(displayName, description, DecimalConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.defaultingDuration(
    displayName: String,
    description: String,
    defaultValue: Duration
): DefaultingConverter<Duration> =
    arg(displayName, description, DurationConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.defaultingEmail(
    displayName: String,
    description: String,
    defaultValue: String
): DefaultingConverter<String> =
    arg(displayName, description, EmailConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.defaultingInt(
    displayName: String,
    description: String,
    defaultValue: Int,
    radix: Int = 10
): DefaultingConverter<Int> =
    arg(displayName, description, IntConverter(radix).toDefaulting(defaultValue))

/**
 * Create a defaulting long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.defaultingLong(
    displayName: String,
    description: String,
    defaultValue: Long,
    radix: Int = 10
): DefaultingConverter<Long> =
    arg(displayName, description, LongConverter(radix).toDefaulting(defaultValue))

/**
 * Create a defaulting whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
@Deprecated(
    "Renamed to defaultingLong",
    replaceWith = ReplaceWith(
        "defaultingLong(displayName, description, defaultValue, radix)",
        "com.kotlindiscord.kord.extensions.commands.converters"
    ),
    level = DeprecationLevel.WARNING
)
public fun Arguments.defaultingNumber(
    displayName: String,
    description: String,
    defaultValue: Long,
    radix: Int = 10
): DefaultingConverter<Long> =
    arg(displayName, description, NumberConverter(radix).toDefaulting(defaultValue))

/**
 * Create a defaulting regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.defaultingRegex(
    displayName: String,
    description: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf()
): DefaultingConverter<Regex> =
    arg(displayName, description, RegexConverter(options).toDefaulting(defaultValue))

/**
 * Create a defaulting string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.defaultingString(
    displayName: String,
    description: String,
    defaultValue: String
): DefaultingConverter<String> =
    arg(displayName, description, StringConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.defaultingT4jDuration(
    displayName: String,
    description: String,
    defaultValue: net.time4j.Duration<IsoUnit>
): DefaultingConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationConverter().toDefaulting(defaultValue))

// endregion

// region: Coalescing converters

/**
 * Create a coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.coalescedDuration(displayName: String, description: String): CoalescingConverter<Duration> =
    arg(displayName, description, DurationCoalescingConverter())

/**
 * Create a coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf()
): CoalescingConverter<Regex> =
    arg(displayName, description, RegexCoalescingConverter(options))

/**
 * Create a coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedString(displayName: String, description: String): CoalescingConverter<String> =
    arg(displayName, description, StringCoalescingConverter())

/**
 * Create a coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedT4jDuration(
    displayName: String,
    description: String
): CoalescingConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationCoalescingConverter())

// endregion

// region: Optional coalescing converters

/**
 * Create an optional coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.optionalCoalescedDuration(
    displayName: String,
    description: String
): OptionalCoalescingConverter<Duration?> =
    arg(displayName, description, DurationCoalescingConverter().toOptional())

/**
 * Create an optional coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf()
): OptionalCoalescingConverter<Regex?> =
    arg(displayName, description, RegexCoalescingConverter(options).toOptional())

/**
 * Create an optional coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedString(
    displayName: String,
    description: String
): OptionalCoalescingConverter<String?> =
    arg(displayName, description, StringCoalescingConverter().toOptional())

/**
 * Create an optional coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedT4jDuration(
    displayName: String,
    description: String
): OptionalCoalescingConverter<net.time4j.Duration<IsoUnit>?> =
    arg(displayName, description, T4JDurationCoalescingConverter().toOptional())

// endregion

// region: Defaulting coalescing converters

/**
 * Create a defaulting coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.defaultingCoalescedDuration(
    displayName: String,
    description: String,
    defaultValue: Duration
): DefaultingCoalescingConverter<Duration> =
    arg(displayName, description, DurationCoalescingConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedRegex(
    displayName: String,
    description: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf()
): DefaultingCoalescingConverter<Regex> =
    arg(displayName, description, RegexCoalescingConverter(options).toDefaulting(defaultValue))

/**
 * Create a defaulting coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedString(
    displayName: String,
    description: String,
    defaultValue: String
): DefaultingCoalescingConverter<String> =
    arg(displayName, description, StringCoalescingConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedT4jDuration(
    displayName: String,
    description: String,
    defaultValue: net.time4j.Duration<IsoUnit>
): DefaultingCoalescingConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationCoalescingConverter().toDefaulting(defaultValue))

// endregion

// region: List converters

/**
 * Create a boolean argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see BooleanConverter
 */
public fun Arguments.booleanList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<Boolean> =
    arg(
        displayName,
        description,
        BooleanConverter().toMulti(required, errorTypeString = "multiple `yes` or `no` values")
    )

/**
 * Create a channel argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see ChannelConverter
 */
public fun Arguments.channelList(
    displayName: String,
    description: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
): MultiConverter<Channel> = arg(
    displayName,
    description,

    ChannelConverter(requireSameGuild, requiredGuild)
        .toMulti(required, signatureTypeString = "channels")
)

/**
 * Create a decimal converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see DecimalConverter
 */
public fun Arguments.decimalList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<Double> =
    arg(displayName, description, DecimalConverter().toMulti(required, signatureTypeString = "decimals"))

/**
 * Create a Java 8 Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see DurationConverter
 */
public fun Arguments.durationList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<Duration> =
    arg(displayName, description, DurationConverter().toMulti(required, signatureTypeString = "durations"))

/**
 * Create an email converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EmailConverter
 */
public fun Arguments.emailList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<String> =
    arg(displayName, description, EmailConverter().toMulti(required))

/**
 * Create an emoji converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EmojiConverter
 */
public fun Arguments.emojiList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<GuildEmoji> =
    arg(displayName, description, EmojiConverter().toMulti(required, signatureTypeString = "server emojis"))

/**
 * Create a guild converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see GuildConverter
 */
public fun Arguments.guildList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<Guild> =
    arg(displayName, description, GuildConverter().toMulti(required, signatureTypeString = "servers"))

/**
 * Create an integer converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see IntConverter
 */
public fun Arguments.intList(
    displayName: String,
    description: String,
    required: Boolean = true,
    radix: Int = 10
): MultiConverter<Int> =
    arg(displayName, description, IntConverter(radix).toMulti(required, signatureTypeString = "numbers"))

/**
 * Create a long converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see LongConverter
 */
public fun Arguments.longList(
    displayName: String,
    description: String,
    required: Boolean = true,
    radix: Int = 10
): MultiConverter<Long> =
    arg(displayName, description, LongConverter(radix).toMulti(required, signatureTypeString = "numbers"))

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
    requiredGuild: (suspend () -> Snowflake)?
): MultiConverter<Member> =
    arg(displayName, description, MemberConverter(requiredGuild).toMulti(required, signatureTypeString = "members"))

/**
 * Create a message converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MessageConverter
 */
public fun Arguments.messageList(
    displayName: String,
    description: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
): MultiConverter<Message> = arg(
    displayName,
    description,
    MessageConverter(requireGuild, requiredGuild)
        .toMulti(required, signatureTypeString = "messages")
)

/**
 * Create a whole number converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see NumberConverter
 */
@Deprecated(
    "Renamed to longList",
    replaceWith = ReplaceWith(
        "longList(displayName, description, required, radix)",
        "com.kotlindiscord.kord.extensions.commands.converters"
    ),
    level = DeprecationLevel.WARNING
)
public fun Arguments.numberList(
    displayName: String,
    description: String,
    required: Boolean = true,
    radix: Int = 10
): MultiConverter<Long> =
    arg(displayName, description, NumberConverter(radix).toMulti(required, signatureTypeString = "numbers"))

/**
 * Create a regex converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RegexConverter
 */
public fun Arguments.regexList(
    displayName: String,
    description: String,
    required: Boolean = true,
    options: Set<RegexOption> = setOf()
): MultiConverter<Regex> =
    arg(displayName, description, RegexConverter(options).toMulti(required, signatureTypeString = "regexes"))

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
    requiredGuild: (suspend () -> Snowflake)?
): MultiConverter<Role> =
    arg(displayName, description, RoleConverter(requiredGuild).toMulti(required, signatureTypeString = "roles"))

/**
 * Create a string converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see StringConverter
 */
public fun Arguments.stringList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<String> =
    arg(displayName, description, StringConverter().toMulti(required))

/**
 * Create a Time4J Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDurationList(
    displayName: String,
    description: String,
    required: Boolean = true
): MultiConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationConverter().toMulti(required, signatureTypeString = "durations"))

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
    required: Boolean = true
): MultiConverter<User> =
    arg(displayName, description, UserConverter().toMulti(required, signatureTypeString = "users"))

// endregion

// region: Enum converters

/**
 * Create an enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    description: String,
    typeName: String,
    noinline getter: suspend (String) -> T?
): SingleConverter<T> = arg(displayName, description, EnumConverter(typeName, getter))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    description: String,
    typeName: String
): SingleConverter<T> =
    enum<T>(displayName, description, typeName, ::getEnum)

/**
 * Create a defaulting enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    description: String,
    typeName: String,
    defaultValue: T,
    noinline getter: suspend (String) -> T?
): DefaultingConverter<T> = arg(displayName, description, EnumConverter(typeName, getter).toDefaulting(defaultValue))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    description: String,
    typeName: String,
    defaultValue: T
): DefaultingConverter<T> =
    defaultingEnum(displayName, description, typeName, defaultValue, ::getEnum)

/**
 * Create an optional enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    description: String,
    typeName: String,
    noinline getter: suspend (String) -> T?
): OptionalConverter<T?> = arg(displayName, description, EnumConverter(typeName, getter).toOptional())

/**
 * Create an optional enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    description: String,
    typeName: String
): OptionalConverter<T?> =
    optionalEnum<T>(displayName, description, typeName, ::getEnum)

/**
 * Create an enum converter, for lists of arguments - using a custom getter.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    description: String,
    typeName: String,
    required: Boolean = true,
    noinline getter: suspend (String) -> T?
): MultiConverter<T> = arg(displayName, description, EnumConverter(typeName, getter).toMulti(required))

/**
 * Create an enum converter, for lists of arguments - using the default getter, [getEnum].
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    description: String,
    typeName: String,
    required: Boolean = true
): MultiConverter<T> =
    enumList<T>(displayName, description, typeName, required, ::getEnum)

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }

// endregion
