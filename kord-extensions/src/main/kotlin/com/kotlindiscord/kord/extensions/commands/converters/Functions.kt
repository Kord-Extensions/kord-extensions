@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.*
import dev.kord.core.entity.channel.Channel
import net.time4j.IsoUnit
import java.time.Duration

// region: Novelty converters

/**
 * Create a union converter, for combining other converters into a single argument - with the caveat of type erasure.
 *
 * This function will automatically remove converters if they were previously registered, so you can use pass it the
 * results of the usual extension functions.
 *
 * @see UnionConverter
 */
public fun Arguments.union(
    displayName: String,
    description: String,
    typeName: String? = null,
    shouldThrow: Boolean = false,
    vararg converters: Converter<*>,
    validator: (suspend Argument<*>.(Any) -> Unit)? = null,
): UnionConverter {
    val converter = UnionConverter(converters.toList(), typeName, shouldThrow, validator)

    converter.validateUnion()

    this.args.toList().forEach {
        if (it.converter in converters) {
            this.args.remove(it)
        }
    }

    arg(displayName, description, converter)

    return converter
}

/**
 * Create an optional union converter, for combining other converters into a single argument - with the caveat of
 * type erasure.
 *
 * This function will automatically remove converters if they were previously registered, so you can use pass it the
 * results of the usual extension functions.
 *
 * @see UnionConverter
 */
public fun Arguments.optionalUnion(
    displayName: String,
    description: String,
    typeName: String? = null,
    shouldThrow: Boolean = false,
    vararg converters: Converter<*>,
    validator: (suspend Argument<*>.(Any?) -> Unit)? = null
): OptionalCoalescingConverter<Any?> {
    val converter = UnionConverter(converters.toList(), typeName, shouldThrow)

    converter.validateUnion()

    this.args.toList().forEach {
        if (it.converter in converters) {
            this.args.remove(it)
        }
    }

    val optionalConverter = converter.toOptional(nestedValidator = validator)

    arg(displayName, description, optionalConverter)

    return optionalConverter
}

// endregion

// region: Required (single) converters

/**
 * Create a boolean argument converter, for single arguments.
 *
 * @see BooleanConverter
 */
public fun Arguments.boolean(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Boolean) -> Unit)? = null
): SingleConverter<Boolean> =
    arg(displayName, description, BooleanConverter(validator))

/**
 * Create a channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.channel(
    displayName: String,
    description: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Channel) -> Unit)? = null,
): SingleConverter<Channel> = arg(
    displayName,
    description,
    ChannelConverter(requireSameGuild, requiredGuild, validator)
)

/**
 * Create a decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.decimal(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Double) -> Unit)? = null,
): SingleConverter<Double> =
    arg(displayName, description, DecimalConverter(validator))

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.duration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): SingleConverter<Duration> =
    arg(displayName, description, DurationConverter(longHelp = longHelp, validator = validator))

/**
 * Create an email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.email(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): SingleConverter<String> =
    arg(displayName, description, EmailConverter(validator))

/**
 * Create an emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.emoji(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(GuildEmoji) -> Unit)? = null,
): SingleConverter<GuildEmoji> =
    arg(displayName, description, EmojiConverter(validator))

/**
 * Create a guild converter, for single arguments.
 *
 * @see GuildConverter
 */
public fun Arguments.guild(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Guild) -> Unit)? = null,
): SingleConverter<Guild> =
    arg(displayName, description, GuildConverter(validator))

/**
 * Create an integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.int(
    displayName: String,
    description: String,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Int) -> Unit)? = null,
): SingleConverter<Int> =
    arg(displayName, description, IntConverter(radix, validator))

/**
 * Create a long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.long(
    displayName: String,
    description: String,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long) -> Unit)? = null,
): SingleConverter<Long> =
    arg(displayName, description, LongConverter(radix, validator))

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
 * Create a message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.message(
    displayName: String,
    description: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Message) -> Unit)? = null,
): SingleConverter<Message> = arg(displayName, description, MessageConverter(requireGuild, requiredGuild, validator))

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
    level = DeprecationLevel.ERROR
)
public fun Arguments.number(
    displayName: String,
    description: String,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long) -> Unit)? = null,
): SingleConverter<Long> =
    arg(displayName, description, NumberConverter(radix, validator))

/**
 * Create a regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.regex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): SingleConverter<Regex> =
    arg(displayName, description, RegexConverter(options, validator))

/**
 * Create a role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.role(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Role) -> Unit)? = null,
): SingleConverter<Role> =
    arg(displayName, description, RoleConverter(requiredGuild, validator))

/**
 * Create a snowflake converter, for single arguments.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.snowflake(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(Snowflake) -> Unit)? = null,
): SingleConverter<Snowflake> =
    arg(displayName, description, SnowflakeConverter(validator))

/**
 * Create a string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.string(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): SingleConverter<String> =
    arg(displayName, description, StringConverter(validator))

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    validator: (suspend Argument<*>.(net.time4j.Duration<IsoUnit>) -> Unit)? = null,
): SingleConverter<net.time4j.Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationConverter(longHelp = longHelp, validator = validator))

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
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Boolean?) -> Unit)? = null,
): OptionalConverter<Boolean?> =
    arg(
        displayName,
        description,
        BooleanConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

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
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Channel?) -> Unit)? = null,
): OptionalConverter<Channel?> =
    arg(
        displayName,
        description,
        ChannelConverter(requireSameGuild, requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.optionalDecimal(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Double?) -> Unit)? = null,
): OptionalConverter<Double?> =
    arg(
        displayName,
        description,
        DecimalConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.optionalDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Duration?) -> Unit)? = null,
): OptionalConverter<Duration?> =
    arg(
        displayName,
        description,
        DurationConverter(longHelp = longHelp)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.optionalEmail(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(String?) -> Unit)? = null,
): OptionalConverter<String?> =
    arg(
        displayName,
        description,
        EmailConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.optionalEmoji(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(GuildEmoji?) -> Unit)? = null,
): OptionalConverter<GuildEmoji?> =
    arg(
        displayName,
        description,
        EmojiConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional guild converter, for single arguments.
 *
 * @see GuildConverter
 */
public fun Arguments.optionalGuild(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Guild?) -> Unit)? = null,
): OptionalConverter<Guild?> =
    arg(
        displayName,
        description,
        GuildConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.optionalInt(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Int?) -> Unit)? = null,
): OptionalConverter<Int?> =
    arg(
        displayName,
        description,
        IntConverter(radix)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.optionalLong(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long?) -> Unit)? = null,
): OptionalConverter<Long?> =
    arg(
        displayName,
        description,
        LongConverter(radix)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

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
 * Create an optional message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.optionalMessage(
    displayName: String,
    description: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Message?) -> Unit)? = null,
): OptionalConverter<Message?> =
    arg(
        displayName,
        description,
        MessageConverter(requireGuild, requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
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
    level = DeprecationLevel.ERROR
)
public fun Arguments.optionalNumber(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long?) -> Unit)? = null,
): OptionalConverter<Long?> =
    arg(
        displayName,
        description,
        NumberConverter(radix)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.optionalRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Regex?) -> Unit)? = null,
): OptionalConverter<Regex?> =
    arg(
        displayName,
        description,
        RegexConverter(options)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional role converter, for single arguments.
 *
 * @see RoleConverter
 */
public fun Arguments.optionalRole(
    displayName: String,
    description: String,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Role?) -> Unit)? = null,
): OptionalConverter<Role?> =
    arg(
        displayName,
        description,
        RoleConverter(requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional snowflake converter, for single arguments.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.optionalSnowflake(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Snowflake?) -> Unit)? = null,
): OptionalConverter<Snowflake?> =
    arg(
        displayName,
        description,
        SnowflakeConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.optionalString(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(String?) -> Unit)? = null,
): OptionalConverter<String?> =
    arg(
        displayName,
        description,
        StringConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.optionalT4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(net.time4j.Duration<IsoUnit>?) -> Unit)? = null,
): OptionalConverter<net.time4j.Duration<IsoUnit>?> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

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
    defaultValue: Boolean,
    validator: (suspend Argument<*>.(Boolean) -> Unit)? = null,
): DefaultingConverter<Boolean> =
    arg(
        displayName,
        description,
        BooleanConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting decimal converter, for single arguments.
 *
 * @see DecimalConverter
 */
public fun Arguments.defaultingDecimal(
    displayName: String,
    description: String,
    defaultValue: Double,
    validator: (suspend Argument<*>.(Double) -> Unit)? = null,
): DefaultingConverter<Double> =
    arg(
        displayName,
        description,
        DecimalConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting Java 8 Duration converter, for single arguments.
 *
 * @see DurationConverter
 */
public fun Arguments.defaultingDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    defaultValue: Duration,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): DefaultingConverter<Duration> =
    arg(
        displayName,
        description,
        DurationConverter(longHelp = longHelp)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.defaultingEmail(
    displayName: String,
    description: String,
    defaultValue: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): DefaultingConverter<String> =
    arg(
        displayName,
        description,
        EmailConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting integer converter, for single arguments.
 *
 * @see IntConverter
 */
public fun Arguments.defaultingInt(
    displayName: String,
    description: String,
    defaultValue: Int,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Int) -> Unit)? = null,
): DefaultingConverter<Int> =
    arg(
        displayName,
        description,
        IntConverter(radix)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting long converter, for single arguments.
 *
 * @see LongConverter
 */
public fun Arguments.defaultingLong(
    displayName: String,
    description: String,
    defaultValue: Long,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long) -> Unit)? = null,
): DefaultingConverter<Long> =
    arg(
        displayName,
        description,
        LongConverter(radix)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

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
    level = DeprecationLevel.ERROR
)
public fun Arguments.defaultingNumber(
    displayName: String,
    description: String,
    defaultValue: Long,
    radix: Int = 10,
    validator: (suspend Argument<*>.(Long) -> Unit)? = null,
): DefaultingConverter<Long> =
    arg(
        displayName,
        description,
        NumberConverter(radix)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting regex converter, for single arguments.
 *
 * @see RegexConverter
 */
public fun Arguments.defaultingRegex(
    displayName: String,
    description: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): DefaultingConverter<Regex> =
    arg(
        displayName,
        description,
        RegexConverter(options)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting snowflake converter, for single arguments.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.defaultingString(
    displayName: String,
    description: String,
    defaultValue: Snowflake,
    validator: (suspend Argument<*>.(Snowflake) -> Unit)? = null,
): DefaultingConverter<Snowflake> =
    arg(
        displayName,
        description,
        SnowflakeConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting string converter, for single arguments.
 *
 * @see StringConverter
 */
public fun Arguments.defaultingString(
    displayName: String,
    description: String,
    defaultValue: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): DefaultingConverter<String> =
    arg(
        displayName,
        description,
        StringConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.defaultingT4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    defaultValue: net.time4j.Duration<IsoUnit>,
    validator: (suspend Argument<*>.(net.time4j.Duration<IsoUnit>) -> Unit)? = null,
): DefaultingConverter<net.time4j.Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

// endregion

// region: Coalescing converters

/**
 * Create a coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.coalescedDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): CoalescingConverter<Duration> =
    arg(
        displayName,
        description,
        DurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow, validator = validator)
    )

/**
 * Create a coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): CoalescingConverter<Regex> =
    arg(displayName, description, RegexCoalescingConverter(options, validator = validator))

/**
 * Create a coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedString(
    displayName:
    String,
    description: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): CoalescingConverter<String> =
    arg(displayName, description, StringCoalescingConverter(validator = validator))

/**
 * Create a coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.coalescedT4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(net.time4j.Duration<IsoUnit>) -> Unit)? = null,
): CoalescingConverter<net.time4j.Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow, validator = validator)
    )

// endregion

// region: Optional coalescing converters

/**
 * Create an optional coalescing Java 8 Duration converter.
 *
 * @see DurationCoalescingConverter
 */
public fun Arguments.optionalCoalescedDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Duration?) -> Unit)? = null,
): OptionalCoalescingConverter<Duration?> =
    arg(
        displayName,
        description,

        DurationCoalescingConverter(longHelp = longHelp, shouldThrow = outputError)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an optional coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedRegex(
    displayName: String,
    description: String,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex?) -> Unit)? = null,
): OptionalCoalescingConverter<Regex?> =
    arg(
        displayName,
        description,

        RegexCoalescingConverter(options).toOptional(nestedValidator = validator)
    )

/**
 * Create an optional coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedString(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(String?) -> Unit)? = null,
): OptionalCoalescingConverter<String?> =
    arg(
        displayName,
        description,

        StringCoalescingConverter().toOptional(nestedValidator = validator)
    )

/**
 * Create an optional coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.optionalCoalescedT4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(net.time4j.Duration<IsoUnit>?) -> Unit)? = null,
): OptionalCoalescingConverter<net.time4j.Duration<IsoUnit>?> =
    arg(
        displayName,
        description,

        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = outputError)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

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
    defaultValue: Duration,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): DefaultingCoalescingConverter<Duration> =
    arg(
        displayName,
        description,
        DurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing regex converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedRegex(
    displayName: String,
    description: String,
    defaultValue: Regex,
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(Regex) -> Unit)? = null,
): DefaultingCoalescingConverter<Regex> =
    arg(
        displayName,
        description,
        RegexCoalescingConverter(options)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing string converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedString(
    displayName: String,
    description: String,
    defaultValue: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): DefaultingCoalescingConverter<String> =
    arg(
        displayName,
        description,
        StringCoalescingConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedT4jDuration(
    displayName: String,
    description: String,
    defaultValue: net.time4j.Duration<IsoUnit>,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(net.time4j.Duration<IsoUnit>) -> Unit)? = null,
): DefaultingCoalescingConverter<net.time4j.Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

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
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Boolean>) -> Unit)? = null,
): MultiConverter<Boolean> =
    arg(
        displayName,
        description,
        BooleanConverter()
            .toMulti(required, errorTypeString = "multiple `yes` or `no` values", nestedValidator = validator)
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
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Channel>) -> Unit)? = null,
): MultiConverter<Channel> = arg(
    displayName,
    description,
    ChannelConverter(requireSameGuild, requiredGuild)
        .toMulti(required, signatureTypeString = "channels", nestedValidator = validator)
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
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Double>) -> Unit)? = null,
): MultiConverter<Double> =
    arg(
        displayName,
        description,
        DecimalConverter().toMulti(required, signatureTypeString = "decimals", nestedValidator = validator)
    )

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
    longHelp: Boolean = true,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Duration>) -> Unit)? = null,
): MultiConverter<Duration> =
    arg(
        displayName,
        description,
        DurationConverter(longHelp = longHelp)
            .toMulti(required, signatureTypeString = "durations", nestedValidator = validator)
    )

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
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<String>) -> Unit)? = null,
): MultiConverter<String> =
    arg(
        displayName,
        description,
        EmailConverter()
            .toMulti(required, nestedValidator = validator)
    )

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
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<GuildEmoji>) -> Unit)? = null,
): MultiConverter<GuildEmoji> =
    arg(
        displayName,
        description,
        EmojiConverter()
            .toMulti(required, signatureTypeString = "server emojis", nestedValidator = validator)
    )

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
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Guild>) -> Unit)? = null,
): MultiConverter<Guild> =
    arg(
        displayName,
        description,
        GuildConverter()
            .toMulti(required, signatureTypeString = "servers", nestedValidator = validator)
    )

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
    radix: Int = 10,
    validator: (suspend Argument<*>.(List<Int>) -> Unit)? = null,
): MultiConverter<Int> =
    arg(
        displayName,
        description,
        IntConverter(radix)
            .toMulti(required, signatureTypeString = "numbers", nestedValidator = validator)
    )

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
    radix: Int = 10,
    validator: (suspend Argument<*>.(List<Long>) -> Unit)? = null,
): MultiConverter<Long> =
    arg(
        displayName,
        description,
        LongConverter(radix)
            .toMulti(required, signatureTypeString = "numbers", nestedValidator = validator)
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
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Message>) -> Unit)? = null,
): MultiConverter<Message> =
    arg(
        displayName,
        description,
        MessageConverter(requireGuild, requiredGuild)
            .toMulti(required, signatureTypeString = "messages", nestedValidator = validator)
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
    level = DeprecationLevel.ERROR
)
public fun Arguments.numberList(
    displayName: String,
    description: String,
    required: Boolean = true,
    radix: Int = 10,
    validator: (suspend Argument<*>.(List<Long>) -> Unit)? = null,
): MultiConverter<Long> =
    arg(
        displayName,
        description,
        NumberConverter(radix)
            .toMulti(required, signatureTypeString = "numbers", nestedValidator = validator)
    )

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
    options: Set<RegexOption> = setOf(),
    validator: (suspend Argument<*>.(List<Regex>) -> Unit)? = null,
): MultiConverter<Regex> =
    arg(
        displayName,
        description,
        RegexConverter(options)
            .toMulti(required, signatureTypeString = "regexes", nestedValidator = validator)
    )

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
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Role>) -> Unit)? = null,
): MultiConverter<Role> =
    arg(
        displayName,
        description,
        RoleConverter(requiredGuild)
            .toMulti(required, signatureTypeString = "roles", nestedValidator = validator)
    )

/**
 * Create a snowflake converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see SnowflakeConverter
 */
public fun Arguments.snowflakeList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Snowflake>) -> Unit)? = null,
): MultiConverter<Snowflake> =
    arg(
        displayName,
        description,
        SnowflakeConverter()
            .toMulti(required, nestedValidator = validator)
    )

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
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<String>) -> Unit)? = null,
): MultiConverter<String> =
    arg(
        displayName,
        description,
        StringConverter()
            .toMulti(required, nestedValidator = validator)
    )

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
    longHelp: Boolean = true,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<net.time4j.Duration<IsoUnit>>) -> Unit)? = null,
): MultiConverter<net.time4j.Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toMulti(required, signatureTypeString = "durations", nestedValidator = validator)
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
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): SingleConverter<T> = arg(displayName, description, EnumConverter(typeName, getter, validator))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): SingleConverter<T> =
    enum(displayName, description, typeName, ::getEnum, validator)

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
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): DefaultingConverter<T> = arg(
    displayName,
    description,
    EnumConverter(typeName, getter)
        .toDefaulting(defaultValue, nestedValidator = validator)
)

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    description: String,
    typeName: String,
    defaultValue: T,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): DefaultingConverter<T> =
    defaultingEnum(displayName, description, typeName, defaultValue, ::getEnum, validator)

/**
 * Create an optional enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    description: String,
    typeName: String,
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(T?) -> Unit)? = null,
): OptionalConverter<T?> = arg(
    displayName,
    description,
    EnumConverter(typeName, getter)
        .toOptional(nestedValidator = validator)
)

/**
 * Create an optional enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: (suspend Argument<*>.(T?) -> Unit)? = null,
): OptionalConverter<T?> =
    optionalEnum<T>(displayName, description, typeName, ::getEnum, validator)

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
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(List<T>) -> Unit)? = null,
): MultiConverter<T> = arg(
    displayName,
    description,
    EnumConverter(typeName, getter)
        .toMulti(required, nestedValidator = validator)
)

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
    required: Boolean = true,
    noinline validator: (suspend Argument<*>.(List<T>) -> Unit)? = null,
): MultiConverter<T> =
    enumList<T>(displayName, description, typeName, required, ::getEnum, validator)

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }

// endregion
