/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.time4j

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
import dev.kordex.core.parsers.DurationParserException
import dev.kordex.core.parsers.InvalidTimeUnitException
import dev.kordex.parser.StringParser
import dev.kordex.parser.tokens.PositionalArgumentToken
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
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
@Converter(
	names = ["t4JDuration"],
	types = [ConverterType.COALESCING, ConverterType.DEFAULTING, ConverterType.OPTIONAL],
	imports = ["net.time4j.*"],

	builderFields = [
		"public var longHelp: Boolean = true",
	],
)
public class T4JDurationCoalescingConverter(
	public val longHelp: Boolean = true,
	shouldThrow: Boolean = false,
	override var validator: Validator<Duration<IsoUnit>> = null,
) : CoalescingConverter<Duration<IsoUnit>>(shouldThrow) {
	override val signatureType: Key = CoreTranslations.Converters.Duration.Error.signatureType

	init {
		bot.settings.aboutBuilder.addCopyright()
	}

	private val logger: KLogger = KotlinLogging.logger {}

	override suspend fun parse(parser: StringParser?, context: CommandContext, named: List<String>?): Int {
		val durations: MutableList<String> = mutableListOf<String>()

		val ignoredWords: List<String> = CoreTranslations.Utils.Durations.ignoredWords
			.withContext(context)
			.translate()
			.split(",")
			.toMutableList()
			.apply { remove(EMPTY_VALUE_STRING) }

		var skipNext: Boolean = false

		val args: List<String> = named ?: parser?.run {
			val tokens: MutableList<String> = mutableListOf()

			while (hasNext) {
				val nextToken: PositionalArgumentToken? = peekNext()

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

			val arg: String = args[index]

			if (arg in ignoredWords) continue

			try {
				// We do it this way so that we stop parsing as soon as an invalid string is found
				T4JDurationParser.parse(arg, context.getLocale())
				T4JDurationParser.parse(durations.joinToString("") + arg, context.getLocale())

				durations.add(arg)
			} catch (e: DurationParserException) {
				try {
					val nextIndex: Int = index + 1

					if (nextIndex >= args.size) {
						throw e
					}

					val nextArg: String = args[nextIndex]
					val combined: String = arg + nextArg

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

	override suspend fun toSlashOption(arg: Argument<*>): OptionWrapper<StringChoiceBuilder> =
		wrapOption(arg.displayName, arg.description) {
			required = true
		}

	override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
		val arg: String = (option as? StringOptionValue)?.value ?: return false

		try {
			this.parsed = T4JDurationParser.parse(arg, context.getLocale())
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
