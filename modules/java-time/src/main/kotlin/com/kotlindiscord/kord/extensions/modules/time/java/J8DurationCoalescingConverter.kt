@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.modules.time.java

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import mu.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime

/**
 * Coalescing argument converter for Java 8 [Duration] arguments.
 *
 * This converter will take individual duration specifiers ("1w", "2y", "3d" etc) until it no longer can, and then
 * combine them into a single [Duration].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 * @param positiveOnly Whether a positive duration is required - `true` by default.
 *
 * @see coalescedDuration
 * @see parseDurationJ8
 */
public class J8DurationCoalescingConverter(
    public val longHelp: Boolean = true,
    public val positiveOnly: Boolean = true,
    shouldThrow: Boolean = false,
    override var validator: (suspend Argument<*>.(ChronoContainer) -> Unit)? = null
) : CoalescingConverter<ChronoContainer>(shouldThrow) {
    override val signatureTypeString: String = "converters.duration.error.signatureType"
    private val logger = KotlinLogging.logger {}

    override suspend fun parse(args: List<String>, context: CommandContext): Int {
        val durations = mutableListOf<String>()
        val ignoredWords = context.translate("utils.durations.ignoredWords").split(",")

        var skipNext = false

        @Suppress("LoopWithTooManyJumpStatements")  // Well you rewrite it then, detekt
        for (index in 0 until args.size) {
            if (skipNext) {
                skipNext = false

                continue
            }

            val arg = args[index]

            if (arg in ignoredWords) continue

            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                J8DurationParser.parse(arg, context.getLocale())
                J8DurationParser.parse(durations.joinToString("") + arg, context.getLocale())

                durations.add(arg)
            } catch (e: DurationParserException) {
                try {
                    val nextIndex = index + 1

                    if (nextIndex >= args.size) {
                        throw e
                    }

                    val nextArg = args[nextIndex]
                    val combined = arg + nextArg

                    J8DurationParser.parse(combined, context.getLocale())
                    J8DurationParser.parse(durations.joinToString("") + combined, context.getLocale())

                    durations.add(combined)
                    skipNext = true
                } catch (t: InvalidTimeUnitException) {
                    throwIfNecessary(t, context)

                    break
                } catch (t: DurationParserException) {
                    throwIfNecessary(t, context)

                    break
                }
            }
        }

        try {
            val result = J8DurationParser.parse(
                durations.joinToString(""),
                context.getLocale()
            )

            if (positiveOnly) {
                val normalized = result.clone()

                normalized.normalize(LocalDateTime.now())

                if (!normalized.isPositive()) {
                    throw CommandException(context.translate("converters.duration.error.positiveOnly"))
                }
            }

            parsed = result
        } catch (e: InvalidTimeUnitException) {
            throwIfNecessary(e, context, true)
        } catch (e: DurationParserException) {
            throwIfNecessary(e, context, true)
        }

        return durations.size
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    private suspend fun throwIfNecessary(
        e: Exception,
        context: CommandContext,
        override: Boolean = false
    ): Unit = if (shouldThrow || override) {
        when (e) {
            is InvalidTimeUnitException -> {
                val message = context.translate(
                    "converters.duration.error.invalidUnit",
                    replacements = arrayOf(e.unit)
                ) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

                throw CommandException(message)
            }

            is DurationParserException -> throw CommandException(e.error)

            else -> throw e
        }
    } else {
        logger.debug(e) { "Error thrown during duration parsing" }
    }
}

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
