/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.i18n.DEFAULT_KORDEX_BUNDLE
import com.kotlindiscord.kord.extensions.i18n.EMPTY_VALUE_STRING
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.parser.tokens.PositionalArgumentToken
import com.kotlindiscord.kord.extensions.parsers.DurationParser
import com.kotlindiscord.kord.extensions.parsers.DurationParserException
import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import kotlinx.datetime.*
import mu.KotlinLogging

/**
 * Argument converter for Kotlin [DateTimePeriod] arguments. You can apply these to an `Instant` using `plus` and a
 * timezone.
 *
 * @param longHelp Whether to send the user a long help message with specific information on how to specify durations.
 * @param positiveOnly Whether a positive duration is required - `true` by default.
 */
@Converter(
    names = ["duration"],
    types = [ConverterType.COALESCING, ConverterType.DEFAULTING, ConverterType.OPTIONAL],
    imports = ["kotlinx.datetime.*"],

    builderFields = [
        "public var longHelp: Boolean = true",
        "public var positiveOnly: Boolean = true",
        "public var shouldThrow: Boolean = false"
    ],
)
public class DurationCoalescingConverter(
    public val longHelp: Boolean = true,
    public val positiveOnly: Boolean = true,
    shouldThrow: Boolean = false,
    override var validator: Validator<DateTimePeriod> = null
) : CoalescingConverter<DateTimePeriod>(shouldThrow) {
    override val signatureTypeString: String = "converters.duration.error.signatureType"
    override val bundle: String = DEFAULT_KORDEX_BUNDLE

    private val logger = KotlinLogging.logger {}

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
        // Check if it's a discord-formatted timestamp first
        val timestamp =
            (named?.getOrNull(0) ?: parser?.peekNext()?.data)?.let { TimestampConverter.parseFromString(it) }
        if (timestamp != null) {
            val result = (timestamp.instant - Clock.System.now()).toDateTimePeriod()

            checkPositive(context, result, positiveOnly)

            this.parsed = result

            return 1
        }

        val durations = mutableListOf<String>()

        val ignoredWords: List<String> = context.translate("utils.durations.ignoredWords")
            .split(",")
            .toMutableList()
            .apply { remove(EMPTY_VALUE_STRING) }

        var skipNext = false

        val args: List<String> = named ?: parser?.run {
            val tokens: MutableList<String> = mutableListOf()

            while (hasNext) {
                val nextToken: PositionalArgumentToken? = peekNext()

                if (nextToken!!.data.all { DurationParser.charValid(it, context.getLocale()) }) {
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

            val arg: String = args[index]

            if (arg in ignoredWords) continue

            try {
                // We do it this way so that we stop parsing as soon as an invalid string is found
                DurationParser.parse(arg, context.getLocale())
                DurationParser.parse(durations.joinToString("") + arg, context.getLocale())

                durations.add(arg)
            } catch (e: DurationParserException) {
                try {
                    val nextIndex: Int = index + 1

                    if (nextIndex >= args.size) {
                        throw e
                    }

                    val nextArg: String = args[nextIndex]
                    val combined: String = arg + nextArg

                    DurationParser.parse(combined, context.getLocale())
                    DurationParser.parse(durations.joinToString("") + combined, context.getLocale())

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
            val result: DateTimePeriod = DurationParser.parse(
                durations.joinToString(""),
                context.getLocale()
            )

            checkPositive(context, result, positiveOnly)

            parsed = result
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
                val message: String = context.translate(
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

    private suspend inline fun checkPositive(context: CommandContext, result: DateTimePeriod, positiveOnly: Boolean) {
        if (positiveOnly) {
            val now: Instant = Clock.System.now()
            val applied: Instant = now.plus(result, TimeZone.UTC)

            if (now > applied) {
                throw DiscordRelayedException(context.translate("converters.duration.error.positiveOnly"))
            }
        }
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false

        try {
            val result: DateTimePeriod = DurationParser.parse(optionValue, context.getLocale())

            if (positiveOnly) {
                val now: Instant = Clock.System.now()
                val applied: Instant = now.plus(result, TimeZone.UTC)

                if (now > applied) {
                    throw DiscordRelayedException(context.translate("converters.duration.error.positiveOnly"))
                }
            }

            parsed = result
        } catch (e: InvalidTimeUnitException) {
            val message: String = context.translate(
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
