/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.commands.converters.impl

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.annotations.converters.Converter
import dev.kordex.core.annotations.converters.ConverterType
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.OptionWrapper
import dev.kordex.core.commands.converters.CoalescingConverter
import dev.kordex.core.commands.converters.Validator
import dev.kordex.core.commands.wrapOption
import dev.kordex.core.i18n.EMPTY_VALUE_STRING
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key
import dev.kordex.core.i18n.withContext
import dev.kordex.core.parsers.DurationParser
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.parser.StringParser
import dev.kordex.parser.tokens.PositionalArgumentToken
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.datetime.*

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
	],
)
public class DurationCoalescingConverter(
	public val longHelp: Boolean = true,
	public val positiveOnly: Boolean = true,
	shouldThrow: Boolean = false,
	override var validator: Validator<DateTimePeriod> = null,
) : CoalescingConverter<DateTimePeriod>(shouldThrow) {
	// TODO: The signature type is not an error!
	override val signatureType: Key = CoreTranslations.Converters.Duration.Error.signatureType

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

		val ignoredWords: List<String> = CoreTranslations.Utils.Durations.ignoredWords
			.withLocale(context.getLocale())
			.translate()
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
		override: Boolean = false,
	): Unit = if (shouldThrow || override) {
		when (e) {
			is InvalidTimeUnitException -> throw DiscordRelayedException(
				if (longHelp) {
					CoreTranslations.Common.paragraphJoiner
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(
							CoreTranslations.Converters.Duration.Error.invalidUnit
								.withLocale(context.getLocale())
								.withOrdinalPlaceholders(e.unit),

							CoreTranslations.Converters.Duration.help
								.withLocale(context.getLocale())
						)
				} else {
					CoreTranslations.Converters.Duration.Error.invalidUnit
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(e.unit)
				}
			)

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
				throw DiscordRelayedException(
					CoreTranslations.Converters.Duration.Error.positiveOnly
						.withContext(context)
				)
			}
		}
	}

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val optionValue = (option as? StringOptionValue)?.value ?: return false

		try {
			val result: DateTimePeriod = DurationParser.parse(optionValue, context.getLocale())

			if (positiveOnly) {
				val now: Instant = Clock.System.now()
				val applied: Instant = now.plus(result, TimeZone.UTC)

				if (now > applied) {
					throw DiscordRelayedException(
						CoreTranslations.Converters.Duration.Error.positiveOnly
							.withContext(context)
					)
				}
			}

			parsed = result
		} catch (e: InvalidTimeUnitException) {
			throw DiscordRelayedException(
				if (longHelp) {
					CoreTranslations.Common.paragraphJoiner
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(
							CoreTranslations.Converters.Duration.Error.invalidUnit
								.withLocale(context.getLocale())
								.withOrdinalPlaceholders(e.unit),

							CoreTranslations.Converters.Duration.help
								.withLocale(context.getLocale())
						)
				} else {
					CoreTranslations.Converters.Duration.Error.invalidUnit
						.withLocale(context.getLocale())
						.withOrdinalPlaceholders(e.unit)
				}
			)
		} catch (e: DurationParserException) {
			throw DiscordRelayedException(e.error)
		}

		return true
	}
}
