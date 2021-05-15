@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview
import java.time.Duration

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.j8Duration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): SingleConverter<Duration> =
    arg(displayName, description, J8DurationConverter(longHelp = longHelp, validator = validator))

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.optionalJ8Duration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Duration?) -> Unit)? = null,
): OptionalConverter<Duration?> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.defaultingJ8Duration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    defaultValue: Duration,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): DefaultingConverter<Duration> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create a Java 8 Duration converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see J8DurationConverter
 */
public fun Arguments.j8DurationList(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<Duration>) -> Unit)? = null,
): MultiConverter<Duration> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp)
            .toMulti(required, signatureTypeString = "durations", nestedValidator = validator)
    )

/**
 * Create a coalescing Java 8 Duration converter.
 *
 * @see J8DurationCoalescingConverter
 */
public fun Arguments.coalescedJ8Duration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(Duration) -> Unit)? = null,
): CoalescingConverter<Duration> =
    arg(
        displayName,
        description,
        J8DurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow, validator = validator)
    )

/**
 * Create an optional coalescing Java 8 Duration converter.
 *
 * @see J8DurationCoalescingConverter
 */
public fun Arguments.optionalCoalescedJ8Duration(
    displayName: String,
    description: String,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Duration?) -> Unit)? = null,
): OptionalCoalescingConverter<Duration?> =
    arg(
        displayName,
        description,

        J8DurationCoalescingConverter(longHelp = longHelp, shouldThrow = outputError)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting coalescing Java 8 Duration converter.
 *
 * @see J8DurationCoalescingConverter
 */
public fun Arguments.defaultingCoalescedJ8Duration(
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
        J8DurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )
