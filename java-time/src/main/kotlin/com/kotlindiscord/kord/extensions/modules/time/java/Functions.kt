@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("StringLiteralDuplication")

package com.kotlindiscord.kord.extensions.modules.time.java

import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import dev.kord.common.annotation.KordPreview

/**
 * Create a Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.j8Duration(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null,
): SingleConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationConverter(
            longHelp = longHelp,
            positiveOnly = requirePositive,
            validator = validator
        )
    )

/**
 * Create an optional Java 8 Duration converter, for single arguments.
 *
 * @see J8DurationConverter
 */
public fun Arguments.optionalJ8Duration(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(ChronoContainer?) -> Unit)? = null,
): OptionalConverter<ChronoContainer?> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp, positiveOnly = requirePositive)
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
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    defaultValue: ChronoContainer,
    validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null,
): DefaultingConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp, positiveOnly = requirePositive)
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
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<ChronoContainer>) -> Unit)? = null,
): MultiConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationConverter(longHelp = longHelp, positiveOnly = requirePositive)
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
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null,
): CoalescingConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationCoalescingConverter(
            longHelp = longHelp,
            shouldThrow = shouldThrow,
            positiveOnly = requirePositive,
            validator = validator
        )
    )

/**
 * Create an optional coalescing Java 8 Duration converter.
 *
 * @see J8DurationCoalescingConverter
 */
public fun Arguments.optionalCoalescedJ8Duration(
    displayName: String,
    description: String,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(ChronoContainer?) -> Unit)? = null,
): OptionalCoalescingConverter<ChronoContainer?> =
    arg(
        displayName,
        description,

        J8DurationCoalescingConverter(longHelp = longHelp, shouldThrow = outputError, positiveOnly = requirePositive)
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
    defaultValue: ChronoContainer,
    requirePositive: Boolean = true,
    longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null,
): DefaultingCoalescingConverter<ChronoContainer> =
    arg(
        displayName,
        description,
        J8DurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow, positiveOnly = requirePositive)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )
