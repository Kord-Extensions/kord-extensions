package com.kotlindiscord.kord.extensions.commands.converters

import com.gitlab.kordlib.common.entity.Snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

// region: Required (single) converters

/**
 * Create a boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
fun Arguments.boolean(displayName: String) =
    arg(displayName, BooleanConverter())

/**
 * Create a channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
fun Arguments.channel(
    displayName: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, ChannelConverter(requireSameGuild, requiredGuild))

/**
 * Create a decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
fun Arguments.decimal(displayName: String) =
    arg(displayName, DecimalConverter())

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
fun Arguments.duration(displayName: String) =
    arg(displayName, DurationConverter())

/**
 * Create an emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
fun Arguments.emoji(displayName: String) =
    arg(displayName, EmojiConverter())

/**
 * Create a guild converter, for single arguments.
 *
 * @see GuildConverter
 */
fun Arguments.guild(displayName: String) =
    arg(displayName, GuildConverter())

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
fun Arguments.member(displayName: String, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(requiredGuild))

/**
 * Create a message converter, for single arguments.
 *
 * @see MessageConverter
 */
fun Arguments.message(
    displayName: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(requireGuild, requiredGuild))

/**
 * Create a whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
fun Arguments.number(displayName: String, radix: Int = 10) =
    arg(displayName, NumberConverter(radix))

/**
 * Create a regex converter, for single arguments.
 *
 * @see RegexConverter
 */
fun Arguments.regex(displayName: String, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(options))

/**
 * Create a role converter, for single arguments.
 *
 * @see RoleConverter
 */
fun Arguments.role(displayName: String, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(requiredGuild))

/**
 * Create a string converter, for single arguments.
 *
 * @see StringConverter
 */
fun Arguments.string(displayName: String) =
    arg(displayName, StringConverter())

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
fun Arguments.t4jDuration(displayName: String) =
    arg(displayName, T4JDurationConverter())

/**
 * Create a user converter, for single arguments.
 *
 * @see UserConverter
 */
fun Arguments.user(displayName: String) =
    arg(displayName, UserConverter())

// endregion

// region: Optional converters

/**
 * Create an optional boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
fun Arguments.optionalBoolean(displayName: String) =
    arg(displayName, BooleanConverter().toOptional())

/**
 * Create an optional channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
fun Arguments.optionalChannel(
    displayName: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, ChannelConverter(requireSameGuild, requiredGuild).toOptional())

/**
 * Create an optional decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
fun Arguments.optionalDecimal(displayName: String) =
    arg(displayName, DecimalConverter().toOptional())

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
fun Arguments.optionalDuration(displayName: String) =
    arg(displayName, DurationConverter().toOptional())

/**
 * Create an optional emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
fun Arguments.optionalEmoji(displayName: String) =
    arg(displayName, EmojiConverter().toOptional())

/**
 * Create an optional guild converter, for single arguments.
 *
 * @see GuildConverter
 */
fun Arguments.optionalGuild(displayName: String) =
    arg(displayName, GuildConverter().toOptional())

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
fun Arguments.optionalMember(displayName: String, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(requiredGuild).toOptional())

/**
 * Create an optional message converter, for single arguments.
 *
 * @see MessageConverter
 */
fun Arguments.optionalMessage(
    displayName: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(requireGuild, requiredGuild).toOptional())

/**
 * Create an optional whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
fun Arguments.optionalNumber(displayName: String, radix: Int = 10) =
    arg(displayName, NumberConverter(radix).toOptional())

/**
 * Create an optional regex converter, for single arguments.
 *
 * @see RegexConverter
 */
fun Arguments.optionalRegex(displayName: String, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(options).toOptional())

/**
 * Create an optional role converter, for single arguments.
 *
 * @see RoleConverter
 */
fun Arguments.optionalRole(displayName: String, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(requiredGuild).toOptional())

/**
 * Create an optional string converter, for single arguments.
 *
 * @see StringConverter
 */
fun Arguments.optionalString(displayName: String) =
    arg(displayName, StringConverter().toOptional())

/**
 * Create an optional Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
fun Arguments.optionalT4jDuration(displayName: String) =
    arg(displayName, T4JDurationConverter().toOptional())

/**
 * Create an optional user converter, for single arguments.
 *
 * @see UserConverter
 */
fun Arguments.optionalUser(displayName: String) =
    arg(displayName, UserConverter().toOptional())

// endregion

// region: Defaulting converters

/**
 * Create a defaulting boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
fun Arguments.defaultingBoolean(displayName: String, defaultValue: Boolean) =
    arg(displayName, BooleanConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
fun Arguments.defaultingDecimal(displayName: String, defaultValue: Double) =
    arg(displayName, DecimalConverter().toDefaulting(defaultValue))

/**
 * Create a defaulting whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
fun Arguments.defaultingNumber(displayName: String, defaultValue: Long, radix: Int = 10) =
    arg(displayName, NumberConverter(radix).toDefaulting(defaultValue))

/**
 * Create a defaulting regex converter, for single arguments.
 *
 * @see RegexConverter
 */
fun Arguments.defaultingRegex(displayName: String, defaultValue: Regex, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(options).toDefaulting(defaultValue))

/**
 * Create a defaulting string converter, for single arguments.
 *
 * @see StringConverter
 */
fun Arguments.defaultingString(displayName: String, defaultValue: String) =
    arg(displayName, StringConverter().toDefaulting(defaultValue))

// endregion

// region: Coalescing converters

/**
 * Create a coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
fun Arguments.coalescedDuration(displayName: String, required: Boolean = true) =
    arg(displayName, DurationCoalescingConverter(required))

/**
 * Create a coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
fun Arguments.coalescedRegex(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexCoalescingConverter(required, options))

/**
 * Create a coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
fun Arguments.coalescedString(displayName: String, required: Boolean = true) =
    arg(displayName, StringCoalescingConverter(required))

/**
 * Create a coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
fun Arguments.coalescedT4jDuration(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationCoalescingConverter(required))

// endregion

// region: List converters

/**
 * Create a boolean argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see BooleanConverter
 */
fun Arguments.booleanList(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanConverter().toMulti(required, errorTypeString = "multiple `yes` or `no` values"))

/**
 * Create a channel argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see ChannelConverter
 */
fun Arguments.channelList(
    displayName: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(
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
fun Arguments.decimalList(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalConverter().toMulti(required, signatureTypeString = "decimals"))

/**
 * Create a Java 8 Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see DurationConverter
 */
fun Arguments.durationList(displayName: String, required: Boolean = true) =
    arg(displayName, DurationConverter().toMulti(required, signatureTypeString = "durations"))

/**
 * Create an emoji converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EmojiConverter
 */
fun Arguments.emojiList(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiConverter().toMulti(required, signatureTypeString = "server emojis"))

/**
 * Create a guild converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see GuildConverter
 */
fun Arguments.guildList(displayName: String, required: Boolean = true) =
    arg(displayName, GuildConverter().toMulti(required, signatureTypeString = "servers"))

/**
 * Create a member converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MemberConverter
 */
fun Arguments.memberList(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(requiredGuild).toMulti(required, signatureTypeString = "members"))

/**
 * Create a message converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MessageConverter
 */
fun Arguments.messageList(
    displayName: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(requireGuild, requiredGuild).toMulti(required, signatureTypeString = "messages"))

/**
 * Create a whole number converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see NumberConverter
 */
fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(radix).toMulti(required, signatureTypeString = "numbers"))

/**
 * Create a regex converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RegexConverter
 */
fun Arguments.regexList(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(options).toMulti(required, signatureTypeString = "regexes"))

/**
 * Create a role converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see RoleConverter
 */
fun Arguments.roleList(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(requiredGuild).toMulti(required, signatureTypeString = "roles"))

/**
 * Create a string converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see StringConverter
 */
fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter().toMulti(required))

/**
 * Create a Time4J Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see T4JDurationConverter
 */
fun Arguments.t4jDurationList(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter().toMulti(required, signatureTypeString = "durations"))

/**
 * Create a user converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see UserConverter
 */
fun Arguments.userList(displayName: String, required: Boolean = true) =
    arg(displayName, UserConverter().toMulti(required, signatureTypeString = "users"))

// endregion

// region: Enum converters

/**
 * Create an enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    typeName: String,
    noinline getter: suspend (String) -> T?
) = arg(displayName, EnumConverter(typeName, getter))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enum(displayName: String, typeName: String) =
    enum<T>(displayName, typeName, ::getEnum)

/**
 * Create a defaulting enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    typeName: String,
    defaultValue: T,
    noinline getter: suspend (String) -> T?
) = arg(displayName, EnumConverter(typeName, getter).toDefaulting(defaultValue))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.defaultingEnum(displayName: String, typeName: String, defaultValue: T) =
    defaultingEnum(displayName, typeName, defaultValue, ::getEnum)

/**
 * Create an optional enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    typeName: String,
    noinline getter: suspend (String) -> T?
) = arg(displayName, EnumConverter(typeName, getter).toOptional())

/**
 * Create an optional enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.optionalEnum(displayName: String, typeName: String) =
    optionalEnum<T>(displayName, typeName, ::getEnum)

/**
 * Create an enum converter, for lists of arguments - using a custom getter.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    typeName: String,
    required: Boolean = true,
    noinline getter: suspend (String) -> T?
) = arg(displayName, EnumConverter(typeName, getter).toMulti(required))

/**
 * Create an enum converter, for lists of arguments - using the default getter, [getEnum].
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enumList(displayName: String, typeName: String, required: Boolean = true) =
    enumList<T>(displayName, typeName, required, ::getEnum)

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
inline fun <reified T : Enum<T>> getEnum(arg: String) =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }

// endregion
