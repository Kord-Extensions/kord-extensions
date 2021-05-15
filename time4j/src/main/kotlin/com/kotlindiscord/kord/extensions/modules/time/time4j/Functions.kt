@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.converters.impl.RegexCoalescingConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Create a Time4J Duration converter, for single arguments.
 *
 * @see T4JDurationConverter
 */
public fun Arguments.t4jDuration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null,
): SingleConverter<Duration<IsoUnit>> =
    arg(displayName, description, T4JDurationConverter(longHelp = longHelp, validator = validator))

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
    validator: (suspend Argument<*>.(Duration<IsoUnit>?) -> Unit)? = null,
): OptionalConverter<Duration<IsoUnit>?> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toOptional(outputError = outputError, nestedValidator = validator)
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
    defaultValue: Duration<IsoUnit>,
    validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null,
): DefaultingConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

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
    validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null,
): CoalescingConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow, validator = validator)
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
    validator: (suspend Argument<*>.(Duration<IsoUnit>?) -> Unit)? = null,
): OptionalCoalescingConverter<Duration<IsoUnit>?> =
    arg(
        displayName,
        description,

        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = outputError)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing Time4J Duration converter.
 *
 * @see RegexCoalescingConverter
 */
public fun Arguments.defaultingCoalescedT4jDuration(
    displayName: String,
    description: String,
    defaultValue: Duration<IsoUnit>,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(Duration<IsoUnit>) -> Unit)? = null,
): DefaultingCoalescingConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow)
            .toDefaulting(defaultValue, nestedValidator = validator)
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
    validator: (suspend Argument<*>.(List<Duration<IsoUnit>>) -> Unit)? = null,
): MultiConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationConverter(longHelp = longHelp)
            .toMulti(required, signatureTypeString = "durations", nestedValidator = validator)
    )
