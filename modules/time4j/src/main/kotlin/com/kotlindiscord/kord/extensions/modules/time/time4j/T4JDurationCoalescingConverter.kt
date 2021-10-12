@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.converters.impl.RegexCoalescingConverter
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import mu.KotlinLogging
import net.time4j.Duration
import net.time4j.IsoUnit

/**
 * Coalescing argument converter for Time4J [Duration] arguments.
 *
 * This converter will take individual duration specifiers ("1w", "2y", "3d" etc) until it no longer can, and then
 * combine them into a single [Duration].
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 *
 * @see coalescedT4jDuration
 * @see parseT4JDuration
 */
public class T4JDurationCoalescingConverter(
    public val longHelp: Boolean = true,
    shouldThrow: Boolean = false,
    override var validator: Validator<Duration<IsoUnit>> = null
) : CoalescingConverter<Duration<IsoUnit>>(shouldThrow) {
    override val signatureTypeString: String = "converters.duration.error.signatureType"
    private val logger = KotlinLogging.logger {}

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
        val durations = mutableListOf<String>()
        val ignoredWords = context.translate("utils.durations.ignoredWords").split(",")

        var skipNext = false

        val args = named ?: parser?.run {
            val tokens: MutableList<String> = mutableListOf()

            while (hasNext) {
                val nextToken = peekNext()

                if (nextToken!!.data.all { T4JDurationParser.charValid(it, context.getLocale()) }) {
                    tokens.add(parseNext()!!.data)
                } else {
                    break
                }
            }

            tokens
        } ?: return 0

        @Suppress("LoopWithTooManyJumpStatements")  // Well you rewrite it then, detekt
        for (index in args.indices) {
            if (skipNext) {
                skipNext = false

                continue
            }

            val arg = args[index]

            if (arg in ignoredWords) continue

            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                T4JDurationParser.parse(arg, context.getLocale())
                T4JDurationParser.parse(durations.joinToString("") + arg, context.getLocale())

                durations.add(arg)
            } catch (e: DurationParserException) {
                try {
                    val nextIndex = index + 1

                    if (nextIndex >= args.size) {
                        throw e
                    }

                    val nextArg = args[nextIndex]
                    val combined = arg + nextArg

                    T4JDurationParser.parse(combined, context.getLocale())
                    T4JDurationParser.parse(durations.joinToString("") + combined, context.getLocale())

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
            parsed = T4JDurationParser.parse(
                durations.joinToString(""),
                context.getLocale()
            )
        } catch (e: InvalidTimeUnitException) {
            throwIfNecessary(e, context, true)
        } catch (e: DurationParserException) {
            throwIfNecessary(e, context, true)
        }

        return durations.size
    }

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

                throw DiscordRelayedException(message)
            }

            is DurationParserException -> throw DiscordRelayedException(e.error)

            else -> throw e
        }
    } else {
        logger.debug(e) { "Error thrown during duration parsing" }
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val arg = (option as? OptionValue.StringOptionValue)?.value ?: return false

        try {
            this.parsed = T4JDurationParser.parse(arg, context.getLocale())
        } catch (e: InvalidTimeUnitException) {
            val message = context.translate(
                "converters.duration.error.invalidUnit",
                replacements = arrayOf(e.unit)
            ) + if (longHelp) "\n\n" + context.translate("converters.duration.help") else ""

            throw DiscordRelayedException(message)
        } catch (e: DurationParserException) {
            throw DiscordRelayedException(e.error)
        }

        return true
    }
}

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
    validator: Validator<Duration<IsoUnit>> = null,
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
    validator: Validator<Duration<IsoUnit>?> = null,
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
    validator: Validator<Duration<IsoUnit>> = null,
): DefaultingCoalescingConverter<Duration<IsoUnit>> =
    arg(
        displayName,
        description,
        T4JDurationCoalescingConverter(longHelp = longHelp, shouldThrow = shouldThrow)
            .toDefaulting(defaultValue, nestedValidator = validator)
    )
