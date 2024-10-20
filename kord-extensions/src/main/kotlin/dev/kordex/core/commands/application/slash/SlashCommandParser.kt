/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress(
	"StringLiteralDuplication" // Needs cleaning up with polymorphism later anyway
)

package dev.kordex.core.commands.application.slash

import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.*
import dev.kordex.core.commands.getDefaultTranslatedDisplayName
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.withContext
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Parser in charge of dealing with the arguments for slash commands.
 *
 * This parser does not support multi converters, as there's no good way to represent them with
 * Discord's API. Coalescing converters will act like single converters.
 */
public open class SlashCommandParser {
	/**
	 * Parse the arguments for this slash command, which have been provided by Discord.
	 *
	 * Instead of taking the objects as Discord provides them, this function will stringify all the command's
	 * arguments. This allows them to be passed through the usual converter system.
	 */
	public suspend fun <T : Arguments> parse(
		builder: () -> T,
		context: SlashCommandContext<*, *, *>,
	): T {
		val argumentsObj = builder.invoke()
		argumentsObj.validate(context.getLocale())

		logger.trace { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

		val args = argumentsObj.args.toMutableList()
		val command = context.event.interaction.command

		val values = command.options.mapValues {
			if (it.value is StringOptionValue) {
				StringOptionValue((it.value.value as String).trim(), it.value.focused)
			} else {
				it.value
			}
		}

		var currentValue: OptionValue<*>?

		@Suppress("LoopWithTooManyJumpStatements")  // Listen here u lil shit
		while (true) {
			val currentArg = args.removeFirstOrNull()
				?: break  // If null, we're out of arguments

			logger.trace { "Current argument: ${currentArg.displayName}" }

			currentValue =
				values[currentArg.getDefaultTranslatedDisplayName()]

			logger.trace { "Current value: $currentValue" }

			@Suppress("TooGenericExceptionCaught")
			when (val converter = currentArg.converter) {
				// It's worth noting that Discord handles validation for required converters, so we don't need to
				// do that checking ourselves, really

				is SingleConverter<*> -> try {
					val parsed = if (currentValue != null) {
						converter.parseOption(context, currentValue)
					} else {
						false
					}

					if (converter.required && !parsed) {
						throw ArgumentParsingException(
							CoreTranslations.ArgumentParser.Error.invalidValue
								.withContext(context)
								.withOrdinalPlaceholders(
									currentArg.displayName
										.withContext(context)
										.translate(),

									converter.getErrorString(context),
									currentValue
								),

							currentArg,
							argumentsObj,
							null
						)
					}

					if (parsed) {
						logger.trace { "Argument ${currentArg.displayName} successfully filled." }

						converter.parseSuccess = true
						currentValue = null

						converter.validate(context)
					}
				} catch (e: DiscordRelayedException) {
					if (converter.required) {
						throw ArgumentParsingException(
							converter.handleError(e, context),

							currentArg,
							argumentsObj,
							null
						)
					}
				} catch (t: Throwable) {
					logger.debug { "Argument ${currentArg.displayName} threw: $t" }

					if (converter.required) {
						throw t
					}
				}

				is CoalescingConverter<*> -> try {
					val parsed = if (currentValue != null) {
						converter.parseOption(context, currentValue)
					} else {
						false
					}

					if (converter.required && !parsed) {
						throw ArgumentParsingException(
							CoreTranslations.ArgumentParser.Error.invalidValue
								.withContext(context)
								.withOrdinalPlaceholders(
									currentArg.displayName
										.withContext(context)
										.translate(),

									converter.getErrorString(context),
									currentValue
								),

							currentArg,
							argumentsObj,
							null
						)
					}

					if (parsed) {
						logger.trace { "Argument ${currentArg.displayName} successfully filled." }

						converter.parseSuccess = true
						currentValue = null

						converter.validate(context)
					}
				} catch (e: DiscordRelayedException) {
					if (converter.required) {
						throw ArgumentParsingException(
							converter.handleError(e, context),

							currentArg,
							argumentsObj,
							null
						)
					}
				} catch (t: Throwable) {
					logger.debug { "Argument ${currentArg.displayName} threw: $t" }

					if (converter.required) {
						throw t
					}
				}

				is OptionalConverter<*> -> try {
					val parsed = if (currentValue != null) {
						converter.parseOption(context, currentValue)
					} else {
						false
					}

					if (parsed) {
						logger.trace { "Argument ${currentArg.displayName} successfully filled." }

						converter.parseSuccess = true
						currentValue = null
					}

					converter.validate(context)
				} catch (e: DiscordRelayedException) {
					if (converter.required || converter.outputError) {
						throw ArgumentParsingException(
							converter.handleError(e, context),

							currentArg,
							argumentsObj,
							null
						)
					}
				} catch (t: Throwable) {
					logger.debug { "Argument ${currentArg.displayName} threw: $t" }

					if (converter.required) {
						throw t
					}
				}

				is OptionalCoalescingConverter<*> -> try {
					val parsed = if (currentValue != null) {
						converter.parseOption(context, currentValue)
					} else {
						false
					}

					if (parsed) {
						logger.trace { "Argument ${currentArg.displayName} successfully filled." }

						converter.parseSuccess = true
						currentValue = null
					}

					converter.validate(context)
				} catch (e: DiscordRelayedException) {
					if (converter.required || converter.outputError) {
						throw ArgumentParsingException(
							converter.handleError(e, context),

							currentArg,
							argumentsObj,
							null
						)
					}
				} catch (t: Throwable) {
					logger.debug { "Argument ${currentArg.displayName} threw: $t" }

					if (converter.required) {
						throw t
					}
				}

				is DefaultingConverter<*> -> try {
					val parsed = if (currentValue != null) {
						converter.parseOption(context, currentValue)
					} else {
						false
					}

					if (parsed) {
						logger.trace { "Argument ${currentArg.displayName} successfully filled." }

						converter.parseSuccess = true
						currentValue = null
					}

					converter.validate(context)
				} catch (e: DiscordRelayedException) {
					if (converter.required || converter.outputError) {
						throw ArgumentParsingException(
							converter.handleError(e, context),

							currentArg,
							argumentsObj,
							null
						)
					}
				} catch (t: Throwable) {
					logger.debug { "Argument ${currentArg.displayName} threw: $t" }

					if (converter.required) {
						throw t
					}
				}

				is DefaultingCoalescingConverter<*> -> try {
					val parsed = if (currentValue != null) {
						converter.parseOption(context, currentValue)
					} else {
						false
					}

					if (parsed) {
						logger.trace { "Argument ${currentArg.displayName} successfully filled." }

						converter.parseSuccess = true
						currentValue = null
					}

					converter.validate(context)
				} catch (e: DiscordRelayedException) {
					if (converter.required || converter.outputError) {
						throw ArgumentParsingException(
							converter.handleError(e, context),

							currentArg,
							argumentsObj,
							null
						)
					}
				} catch (t: Throwable) {
					logger.debug { "Argument ${currentArg.displayName} threw: $t" }

					if (converter.required) {
						throw t
					}
				}

				else -> error("Unsupported type for converter: $converter")
			}
		}

		return argumentsObj
	}
}
