package com.kotlindiscord.kord.extensions.commands.converters

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.*
import com.gitlab.kordlib.core.entity.channel.Channel
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import net.time4j.IsoUnit
import java.time.Duration

// region: Required (single) converters

/**
 * Create a boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.boolean(displayName: String): SingleConverter<Boolean> =
    arg(displayName, BooleanConverter())

/**
 * Create a channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.channel(
    displayName: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
): SingleConverter<Channel> = arg(displayName, ChannelConverter(requireSameGuild, requiredGuild))

/**
 * Create a decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.decimal(displayName: String): SingleConverter<Double> =
    arg(displayName, DecimalConverter())

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.duration(displayName: String): SingleConverter<Duration> =
    arg(displayName, DurationConverter())

/**
 * Create an emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.emoji(displayName: String): SingleConverter<GuildEmoji> =
    arg(displayName, EmojiConverter())

/**
 * Create a guild converter, for single arguments.
 *
 * @see GuildConverter
 */
public fun Arguments.guild(displayName: String): SingleConverter<Guild> =
    arg(displayName, GuildConverter())

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
public fun Arguments.member(displayName: String, requiredGuild: (suspend () -> Snowflake)?): SingleConverter<Member> =
    arg(displayName, MemberConverter(requiredGuild))

/**
 * Create a message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.message(
    displayName: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
): SingleConverter<Message> = arg(displayName, MessageConverter(requireGuild, requiredGuild))

/**
 * Create a whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
public fun Arguments.number(displayName: String, radix: Int = 10): SingleConverter<Long> =
    arg(displayName, NumberConverter(radix))

/**
 * Create a regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.regex(displayName: String, options: Set<RegexOption> = setOf()): SingleConverter<Regex> =
    arg(displayName, RegexConverter(options))

/**
 * Create a role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.role(displayName: String, requiredGuild: (suspend () -> Snowflake)?): SingleConverter<Role> =
    arg(displayName, RoleConverter(requiredGuild))

/**
 * Create a string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.string(displayName: String): SingleConverter<String> =
    arg(displayName, StringConverter())

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDuration(displayName: String): SingleConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, T4JDurationConverter())

/**
 * Create a user converter, for single arguments.
 *
 * @see UserConverter
 */
public fun Arguments.user(displayName: String): SingleConverter<User> =
    arg(displayName, UserConverter())

// endregion

// region: Optional converters

/**
 * Create an optional boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.optionalBoolean(displayName: String): OptionalConverter<Boolean?> =
    arg(displayName, BooleanConverter().toOptional())

/**
 * Create an optional channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.optionalChannel(
    displayName: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
): OptionalConverter<Channel?> = arg(displayName, ChannelConverter(requireSameGuild, requiredGuild).toOptional())

/**
 * Create an optional decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.optionalDecimal(displayName: String): OptionalConverter<Double?> =
    arg(displayName, DecimalConverter().toOptional())

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.optionalDuration(displayName: String): OptionalConverter<Duration?> =
    arg(displayName, DurationConverter().toOptional())

/**
 * Create an optional emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.optionalEmoji(displayName: String): OptionalConverter<GuildEmoji?> =
    arg(displayName, EmojiConverter().toOptional())

/**
 * Create an optional guild converter, for single arguments.
 *
 * @see GuildConverter
 */
public fun Arguments.optionalGuild(displayName: String): OptionalConverter<Guild?> =
    arg(displayName, GuildConverter().toOptional())

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
public fun Arguments.optionalMember(
    displayName: String,
    requiredGuild: (suspend () -> Snowflake)?
): OptionalConverter<Member?> =
    arg(displayName, MemberConverter(requiredGuild).toOptional())

/**
 * Create an optional message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.optionalMessage(
    displayName: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
): OptionalConverter<Message?> = arg(displayName, MessageConverter(requireGuild, requiredGuild).toOptional())

/**
 * Create an optional whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
public fun Arguments.optionalNumber(displayName: String, radix: Int = 10): OptionalConverter<Long?> =
    arg(displayName, NumberConverter(radix).toOptional())

/**
 * Create an optional regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.optionalRegex(
    displayName: String,
    options: Set<RegexOption> = setOf()
): OptionalConverter<Regex?> =
    arg(displayName, RegexConverter(options).toOptional())

/**
 * Create an optional role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.optionalRole(
    displayName: String,
    requiredGuild: (suspend () -> Snowflake)?
): OptionalConverter<Role?> =
    arg(displayName, RoleConverter(requiredGuild).toOptional())

/**
 * Create an optional string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.optionalString(displayName: String): OptionalConverter<String?> =
    arg(displayName, StringConverter().toOptional())

/**
 * Create an optional Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.optionalT4jDuration(displayName: String): OptionalConverter<net.time4j.Duration<IsoUnit>?> =
    arg(displayName, T4JDurationConverter().toOptional())

/**
 * Create an optional user converter, for single arguments.
 *
 * @see UserConverter
 */
public fun Arguments.optionalUser(displayName: String): OptionalConverter<User?> =
    arg(displayName, UserConverter().toOptional())

// endregion

// region: Defaulting converters

/**
 * Create a defaulting boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.defaultingBoolean(displayName: String, defaultValue: Boolean): DefaultingConverter<Boolean> =
    arg(displayName, BooleanConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.defaultingDecimal(displayName: String, defaultValue: Double): DefaultingConverter<Double> =
    arg(displayName, DecimalConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.defaultingDuration(displayName: String, defaultValue: Duration): DefaultingConverter<Duration> =
    arg(displayName, DurationConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
public fun Arguments.defaultingNumber(
    displayName: String,
    defaultValue: Long,
    radix: Int = 10
): DefaultingConverter<Long> =
    arg(displayName, NumberConverter(radix).toDefaulting(defaultValue))

/**
 * Create a defaulting regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.defaultingRegex(
    displayName: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf()
): DefaultingConverter<Regex> =
    arg(displayName, RegexConverter(options).toDefaulting(defaultValue))

/**
 * Create a defaulting string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.defaultingString(displayName: String, defaultValue: String): DefaultingConverter<String> =
    arg(displayName, StringConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.defaultingT4jDuration(
    displayName: String,
    defaultValue: net.time4j.Duration<IsoUnit>
): DefaultingConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, T4JDurationConverter().toDefaulting(defaultValue))

// endregion

// region: Coalescing converters

/**
 * Create a coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.coalescedDuration(displayName: String): CoalescingConverter<Duration> =
    arg(displayName, DurationCoalescingConverter())

/**
 * Create a coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedRegex(
    displayName: String,
    options: Set<RegexOption> = setOf()
): CoalescingConverter<Regex> =
    arg(displayName, RegexCoalescingConverter(options))

/**
 * Create a coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedString(displayName: String): CoalescingConverter<String> =
    arg(displayName, StringCoalescingConverter())

/**
 * Create a coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedT4jDuration(displayName: String): CoalescingConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, T4JDurationCoalescingConverter())

// endregion

// region: Optional coalescing converters

/**
 * Create an optional coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.optionalCoalescedDuration(displayName: String): OptionalCoalescingConverter<Duration?> =
    arg(displayName, DurationCoalescingConverter().toOptional())

/**
 * Create an optional coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedRegex(
    displayName: String,
    options: Set<RegexOption> = setOf()
): OptionalCoalescingConverter<Regex?> =
    arg(displayName, RegexCoalescingConverter(options).toOptional())

/**
 * Create an optional coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedString(displayName: String): OptionalCoalescingConverter<String?> =
    arg(displayName, StringCoalescingConverter().toOptional())

/**
 * Create an optional coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedT4jDuration(
    displayName: String
): OptionalCoalescingConverter<net.time4j.Duration<IsoUnit>?> =
    arg(displayName, T4JDurationCoalescingConverter().toOptional())

// endregion

// region: Defaulting coalescing converters

/**
 * Create a defaulting coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.defaultingCoalescedDuration(
    displayName: String,
    defaultValue: Duration
): DefaultingCoalescingConverter<Duration> =
    arg(displayName, DurationCoalescingConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedRegex(
    displayName: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf()
): DefaultingCoalescingConverter<Regex> =
    arg(displayName, RegexCoalescingConverter(options).toDefaulting(defaultValue))

/**
 * Create a defaulting coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedString(
    displayName: String,
    defaultValue: String
): DefaultingCoalescingConverter<String> =
    arg(displayName, StringCoalescingConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedT4jDuration(
    displayName: String,
    defaultValue: net.time4j.Duration<IsoUnit>
): DefaultingCoalescingConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, T4JDurationCoalescingConverter().toDefaulting(defaultValue))

// endregion

// region: List converters

/**
 * Create a boolean argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see BooleanConverter
 */
public fun Arguments.booleanList(displayName: String, required: Boolean = true): MultiConverter<Boolean> =
    arg(displayName, BooleanConverter().toMulti(required, errorTypeString = "multiple `yes` or `no` values"))

/**
 * Create a channel argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see ChannelConverter
 */
public fun Arguments.channelList(
    displayName: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
): MultiConverter<Channel> = arg(
    displayName,

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
public fun Arguments.decimalList(displayName: String, required: Boolean = true): MultiConverter<Double> =
    arg(displayName, DecimalConverter().toMulti(required, signatureTypeString = "decimals"))

/**
 * Create a Java 8 Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see DurationConverter
 */
public fun Arguments.durationList(displayName: String, required: Boolean = true): MultiConverter<Duration> =
    arg(displayName, DurationConverter().toMulti(required, signatureTypeString = "durations"))

/**
 * Create an emoji converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EmojiConverter
 */
public fun Arguments.emojiList(displayName: String, required: Boolean = true): MultiConverter<GuildEmoji> =
    arg(displayName, EmojiConverter().toMulti(required, signatureTypeString = "server emojis"))

/**
 * Create a guild converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see GuildConverter
 */
public fun Arguments.guildList(displayName: String, required: Boolean = true): MultiConverter<Guild> =
    arg(displayName, GuildConverter().toMulti(required, signatureTypeString = "servers"))

/**
 * Create a member converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MemberConverter
 */
public fun Arguments.memberList(
    displayName: String,
    required: Boolean,
    requiredGuild: (suspend () -> Snowflake)?
): MultiConverter<Member> =
    arg(displayName, MemberConverter(requiredGuild).toMulti(required, signatureTypeString = "members"))

/**
 * Create a message converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MessageConverter
 */
public fun Arguments.messageList(
    displayName: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
): MultiConverter<Message> = arg(
    displayName,
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
public fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10): MultiConverter<Long> =
    arg(displayName, NumberConverter(radix).toMulti(required, signatureTypeString = "numbers"))

/**
 * Create a regex converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RegexConverter
 */
public fun Arguments.regexList(
    displayName: String,
    required: Boolean = true,
    options: Set<RegexOption> = setOf()
): MultiConverter<Regex> =
    arg(displayName, RegexConverter(options).toMulti(required, signatureTypeString = "regexes"))

/**
 * Create a role converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RoleConverter
 */
public fun Arguments.roleList(
    displayName: String,
    required: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)?
): MultiConverter<Role> =
    arg(displayName, RoleConverter(requiredGuild).toMulti(required, signatureTypeString = "roles"))

/**
 * Create a string converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see StringConverter
 */
public fun Arguments.stringList(displayName: String, required: Boolean = true): MultiConverter<String> =
    arg(displayName, StringConverter().toMulti(required))

/**
 * Create a Time4J Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDurationList(
    displayName: String,
    required: Boolean = true
): MultiConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, T4JDurationConverter().toMulti(required, signatureTypeString = "durations"))

/**
 * Create a user converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see UserConverter
 */
public fun Arguments.userList(displayName: String, required: Boolean = true): MultiConverter<User> =
    arg(displayName, UserConverter().toMulti(required, signatureTypeString = "users"))

// endregion

// region: Enum converters

/**
 * Create an enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    typeName: String,
    noinline getter: suspend (String) -> T?
): SingleConverter<T> = arg(displayName, EnumConverter(typeName, getter))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(displayName: String, typeName: String): SingleConverter<T> =
    enum<T>(displayName, typeName, ::getEnum)

/**
 * Create a defaulting enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    typeName: String,
    defaultValue: T,
    noinline getter: suspend (String) -> T?
): DefaultingConverter<T> = arg(displayName, EnumConverter(typeName, getter).toDefaulting(defaultValue))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    typeName: String,
    defaultValue: T
): DefaultingConverter<T> =
    defaultingEnum(displayName, typeName, defaultValue, ::getEnum)

/**
 * Create an optional enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    typeName: String,
    noinline getter: suspend (String) -> T?
): OptionalConverter<T?> = arg(displayName, EnumConverter(typeName, getter).toOptional())

/**
 * Create an optional enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    typeName: String
): OptionalConverter<T?> =
    optionalEnum<T>(displayName, typeName, ::getEnum)

/**
 * Create an enum converter, for lists of arguments - using a custom getter.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    typeName: String,
    required: Boolean = true,
    noinline getter: suspend (String) -> T?
): MultiConverter<T> = arg(displayName, EnumConverter(typeName, getter).toMulti(required))

/**
 * Create an enum converter, for lists of arguments - using the default getter, [getEnum].
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    typeName: String,
    required: Boolean = true
): MultiConverter<T> =
    enumList<T>(displayName, typeName, required, ::getEnum)

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }

// endregion
