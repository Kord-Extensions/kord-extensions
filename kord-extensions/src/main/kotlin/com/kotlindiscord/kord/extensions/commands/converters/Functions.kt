package com.kotlindiscord.kord.extensions.commands.converters

import com.gitlab.kordlib.common.entity.Snowflake
import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

/**
 * Create a boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
fun Arguments.boolean(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanConverter(required))

/**
 * Create a boolean argument converter, for lists of arguments.
 *
 * @see BooleanConverter
 */
fun Arguments.booleanList(displayName: String, required: Boolean = true) =
    arg(displayName, BooleanConverter(required).toMulti(errorTypeString = "multiple `yes` or `no` values"))

/**
 * Create a channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
fun Arguments.channel(
    displayName: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, ChannelConverter(required, requireSameGuild, requiredGuild))

/**
 * Create a channel argument converter, for lists of arguments.
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

    ChannelConverter(required, requireSameGuild, requiredGuild)
        .toMulti(signatureTypeString = "channels")
)

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

/**
 * Create a decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
fun Arguments.decimal(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalConverter(required))

/**
 * Create a decimal converter, for lists of arguments.
 *
 * @see DecimalConverter
 */
fun Arguments.decimalList(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalConverter(required).toMulti(signatureTypeString = "decimals"))

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
fun Arguments.duration(displayName: String, required: Boolean = true) =
    arg(displayName, DurationConverter(required))

/**
 * Create a Java 8 Duration converter, for lists of arguments.
 *
 * @see DurationConverter
 */
fun Arguments.durationList(displayName: String, required: Boolean = true) =
    arg(displayName, DurationConverter(required).toMulti(signatureTypeString = "durations"))

/**
 * Create an emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
fun Arguments.emoji(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiConverter(required))

/**
 * Create an emoji converter, for lists of arguments.
 *
 * @see EmojiConverter
 */
fun Arguments.emojiList(displayName: String, required: Boolean = true) =
    arg(displayName, EmojiConverter(required).toMulti(signatureTypeString = "server emojis"))

/**
 * Create an enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    typeName: String,
    required: Boolean = true,
    noinline getter: suspend (String) -> T?
) = arg(displayName, EnumConverter<T>(required, typeName, getter))

/**
* Create an enum converter, for single arguments - using the default getter, [getEnum].
*
* @see EnumConverter
*/
inline fun <reified T : Enum<T>> Arguments.enum(displayName: String, typeName: String, required: Boolean = true) =
    enum<T>(displayName, typeName, required, ::getEnum)

/**
 * Create an enum converter, for lists of arguments - using a custom getter..
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    typeName: String,
    required: Boolean = true,
    noinline getter: suspend (String) -> T?
) = arg(displayName, EnumConverter<T>(required, typeName, getter).toMulti())

/**
 * Create an enum converter, for lists of arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
inline fun <reified T : Enum<T>> Arguments.enumList(displayName: String, typeName: String, required: Boolean = true) =
    enumList<T>(displayName, typeName, required, ::getEnum)

/**
 * Create a guild converter, for single arguments.
 *
 * @see GuildConverter
 */
fun Arguments.guild(displayName: String, required: Boolean = true) =
    arg(displayName, GuildConverter(required))

/**
 * Create a guild converter, for lists of arguments.
 *
 * @see GuildConverter
 */
fun Arguments.guildList(displayName: String, required: Boolean = true) =
    arg(displayName, GuildConverter(required).toMulti(signatureTypeString = "servers"))

/**
 * Create a member converter, for single arguments.
 *
 * @see MemberConverter
 */
fun Arguments.member(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(required, requiredGuild))

/**
 * Create a member converter, for lists of arguments.
 *
 * @see MemberConverter
 */
fun Arguments.memberList(displayName: String, required: Boolean, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, MemberConverter(required, requiredGuild).toMulti(signatureTypeString = "members"))

/**
 * Create a message converter, for single arguments.
 *
 * @see MessageConverter
 */
fun Arguments.message(
    displayName: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(required, requireGuild, requiredGuild))

/**
 * Create a message converter, for lists of arguments.
 *
 * @see MessageConverter
 */
fun Arguments.messageList(
    displayName: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null
) = arg(displayName, MessageConverter(required, requireGuild, requiredGuild).toMulti(signatureTypeString = "messages"))

/**
 * Create a whole number converter, for single arguments.
 *
 * @see NumberConverter
 */
fun Arguments.number(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix))

/**
 * Create a whole number converter, for lists of arguments.
 *
 * @see NumberConverter
 */
fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix).toMulti(signatureTypeString = "numbers"))

/**
 * Create a regex converter, for single arguments.
 *
 * @see RegexConverter
 */
fun Arguments.regex(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(required, options))

/**
 * Create a regex converter, for lists of arguments.
 *
 * @see RegexConverter
 */
fun Arguments.regexList(displayName: String, required: Boolean = true, options: Set<RegexOption> = setOf()) =
    arg(displayName, RegexConverter(required, options).toMulti(signatureTypeString = "regexes"))

/**
 * Create a role converter, for single arguments.
 *
 * @see RoleConverter
 */
fun Arguments.role(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(required, requiredGuild))

/**
 * Create a role converter, for lists of arguments.
 *
 * @see RoleConverter
 */
fun Arguments.roleList(displayName: String, required: Boolean = true, requiredGuild: (suspend () -> Snowflake)?) =
    arg(displayName, RoleConverter(required, requiredGuild).toMulti(signatureTypeString = "roles"))

/**
 * Create a string converter, for single arguments.
 *
 * @see StringConverter
 */
fun Arguments.string(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required))

/**
 * Create a string converter, for lists of arguments.
 *
 * @see StringConverter
 */
fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required).toMulti())

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
fun Arguments.t4jDuration(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter(required))

/**
 * Create a Time4J Duration converter, for lists of arguments.
 *
 * @see T4JDurationConverter
 */
fun Arguments.t4jDurationList(displayName: String, required: Boolean = true) =
    arg(displayName, T4JDurationConverter(required).toMulti(signatureTypeString = "durations"))

/**
 * Create a user converter, for single arguments.
 *
 * @see UserConverter
 */
fun Arguments.user(displayName: String, required: Boolean = true) =
    arg(displayName, UserConverter(required))

/**
 * Create a user converter, for lists of arguments.
 *
 * @see UserConverter
 */
fun Arguments.userList(displayName: String, required: Boolean = true) =
    arg(displayName, UserConverter(required).toMulti(signatureTypeString = "users"))

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
inline fun <reified T : Enum<T>> getEnum(arg: String) =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
