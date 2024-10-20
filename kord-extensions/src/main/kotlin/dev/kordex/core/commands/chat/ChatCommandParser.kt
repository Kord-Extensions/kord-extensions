/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress(
	"TooGenericExceptionCaught",
	"StringLiteralDuplication",
	"DuplicatedCode"
)

package dev.kordex.core.commands.chat

import dev.kordex.core.ArgumentParsingException
import dev.kordex.core.DiscordRelayedException
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.commands.Argument
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.CommandContext
import dev.kordex.core.commands.converters.*
import dev.kordex.core.commands.converters.types.MultiNamedInputConverter
import dev.kordex.core.commands.converters.types.SingleNamedInputConverter
import dev.kordex.core.commands.getDefaultTranslatedDisplayName
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.withContext
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.parser.StringParser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject
import java.util.*
import kotlin.collections.set

private val logger = KotlinLogging.logger {}

/**
 * Class in charge of handling argument parsing for commands.
 *
 * Argument parsing is a tricky beast. This class employs multiple strategies in order to try to keep argument parsing
 * as intuitive as possible, without breaking expectations too much. It supports both positional and keyword arguments,
 * plus optional and required arguments and comprehensive error handling.
 *
 * Please note: The order of arguments in your [Arguments] subclass matters. Converters are always run in the order
 * they're defined, and positional arguments are also parsed following this order. This means that converters
 * that take lambdas as constructor parameters are able to rely on the values provided by previously parsed arguments.
 *
 * We recommend reading over the source code if you'd like to get to grips with how this all works.
 */
public open class ChatCommandParser : KordExKoinComponent {
	/** Current instance of the bot. **/
	public open val bot: ExtensibleBot by inject()

	public open suspend fun doParse(
		arguments: Arguments,
		argument: Argument<*>,
		parser: StringParser,
		context: ChatCommandContext<*>,
		kwArgs: MutableList<String>?,
	): Any {
		if (kwArgs != null && kwArgs.size != 1) {
			throw ArgumentParsingException(
				reason = CoreTranslations.ArgumentParser.Error.requiresOneValue
					.withContext(context)
					.withOrdinalPlaceholders(argument.displayName, kwArgs.size),

				argument = argument,
				arguments = arguments,
				parser = parser
			)
		}

		return try {
			when (val c = argument.converter) {
				is SingleNamedInputConverter<*, *, *> -> c.parse(parser, context, kwArgs?.first())
				is MultiNamedInputConverter<*, *, *> -> c.parse(parser, context, kwArgs)

				else -> {
					logger.error {
						"Converter $c doesn't implement SingleNamedInputConverter or MultiNamedInputConverter"
					}

					throw ArgumentParsingException(
						CoreTranslations.ArgumentParser.Error.errorInArgument
							.withContext(context)
							.withOrdinalPlaceholders(
								argument.displayName,

								CoreTranslations.ArgumentParser.Error.unknownConverterType
									.withOrdinalPlaceholders(c)
							),

						argument,
						arguments,
						parser
					)
				}
			}
		} catch (e: Exception) {
			e
		}
	}

	public open suspend fun handleThrowable(
		arguments: Arguments,
		argument: Argument<*>,
		parser: StringParser,
		context: ChatCommandContext<*>,
		kwArgs: MutableList<String>?,
		throwable: Throwable,
	) {
		val hasKwargs = kwArgs != null

		val doRelay = when (val c = argument.converter) {
			is SingleConverter<*> -> c.required || hasKwargs

			is DefaultingConverter<*> -> (c.required || c.outputError || hasKwargs) &&
				throwable is DiscordRelayedException

			is OptionalConverter<*> -> if (throwable is DiscordRelayedException) {
				c.required || c.outputError || hasKwargs
			} else {
				c.required || hasKwargs
			}

			is ListConverter<*> -> c.required
			is CoalescingConverter<*> -> c.required
			is OptionalCoalescingConverter<*> -> c.required || c.outputError || hasKwargs

			is DefaultingCoalescingConverter<*> -> if (throwable is DiscordRelayedException) {
				c.required || c.outputError || hasKwargs
			} else {
				c.required || hasKwargs
			}

			else -> {
				logger.error { "Converter with unknown base type: $c" }

				throw ArgumentParsingException(
					CoreTranslations.ArgumentParser.Error.errorInArgument
						.withContext(context)
						.withOrdinalPlaceholders(
							argument.displayName,

							CoreTranslations.ArgumentParser.Error.unknownConverterType
								.withOrdinalPlaceholders(c)
						),

					argument,
					arguments,
					parser
				)
			}
		}

		if (!doRelay) {
			return
		}

		when (val t = throwable) {
			is ArgumentParsingException -> throw t

			is DiscordRelayedException -> throw ArgumentParsingException(
					CoreTranslations.ArgumentParser.Error.errorInArgument
						.withContext(context)
						.withOrdinalPlaceholders(
							argument.displayName,
							argument.converter.handleError(t, context)
						),

					argument,
					arguments,
					parser
				)

			else -> {
				logger.warn(t) { "Exception thrown by argument: ${argument.displayName.key}" }

				throw t
			}
		}
	}

	public suspend fun throwInvalidValue(
		arguments: Arguments,
		argument: Argument<*>,
		parser: StringParser,
		context: ChatCommandContext<*>,
	): Nothing {
		val c = argument.converter

		throw ArgumentParsingException(
			CoreTranslations.ArgumentParser.Error.invalidValue
				.withContext(context)
				.withOrdinalPlaceholders(
					argument.displayName,

					c.getErrorKey()
				),

			argument,
			arguments,
			parser
		)
	}

	public suspend fun throwNotAllValid(
		arguments: Arguments,
		argument: Argument<*>,
		parser: StringParser,
		context: ChatCommandContext<*>,
		numArgs: Int,
		numParsed: Int,
	): Nothing {
		val c = argument.converter

		throw ArgumentParsingException(
			CoreTranslations.ArgumentParser.Error.notAllValid
				.withContext(context)
				.withOrdinalPlaceholders(
					argument.displayName,

					numArgs,
					numParsed,
					c.signatureType
				),

			argument,
			arguments,
			parser
		)
	}

	public open suspend fun checkResult(
		arguments: Arguments,
		argument: Argument<*>,
		parser: StringParser,
		context: ChatCommandContext<*>,
		kwArgs: MutableList<String>?,
		result: Any,
	) {
		val hasKwargs = kwArgs != null

		when (val c = argument.converter) {
			is SingleConverter<*> ->  {
				result as Boolean

				if ((c.required || hasKwargs) && !result) {
					throwInvalidValue(arguments, argument, parser, context)
				}

				if (result) {
					logger.trace { "Argument ${argument.displayName} successfully filled." }

					c.parseSuccess = true

					c.validate(context)
				}
			}

			is DefaultingConverter<*> -> {
				result as Boolean

				if (result) {
					logger.trace { "Successfully filled argument: ${argument.displayName.key}" }

					c.parseSuccess = true
				}

				c.validate(context)
			}

			is OptionalConverter<*> -> {
				result as Boolean

				if (result) {
					logger.trace { "Successfully filled argument: ${argument.displayName.key}" }

					c.parseSuccess = true
				}

				c.validate(context)
			}

			is ListConverter<*> -> {
				result as Int

				if ((c.required || hasKwargs) && result < 1) {
					throwInvalidValue(arguments, argument, parser, context)
				}

				if (hasKwargs) {
					if (result < kwArgs.size) {
						throwNotAllValid(arguments, argument, parser, context, kwArgs.size, result)
					}

					c.parseSuccess = true
				} else if (result > 0) {
					logger.trace { "Successfully filled argument: ${argument.displayName.key}" }

					c.parseSuccess = true
				}

				c.validate(context)
			}

			is CoalescingConverter<*> -> {
				result as Int

				if ((c.required || hasKwargs) && result < 1) {
					throwInvalidValue(arguments, argument, parser, context)
				}

				if (hasKwargs) {
					if (result < kwArgs.size) {
						throwNotAllValid(arguments, argument, parser, context, kwArgs.size, result)
					}

					c.parseSuccess = true
					c.validate(context)
				} else if (result > 0) {
					logger.trace { "Successfully filled argument: ${argument.displayName.key}" }

					c.parseSuccess = true
					c.validate(context)
				}
			}

			is OptionalCoalescingConverter<*> -> {
				result as Int

				if ((c.required || hasKwargs) && result < 1) {
					throwInvalidValue(arguments, argument, parser, context)
				}

				if (hasKwargs) {
					if (result < kwArgs.size) {
						throwNotAllValid(arguments, argument, parser, context, kwArgs.size, result)
					}

					c.parseSuccess = true
				} else if (result > 0) {
					logger.trace { "Successfully filled argument: ${argument.displayName.key}" }

					c.parseSuccess = true
				}

				c.validate(context)
			}

			is DefaultingCoalescingConverter<*> -> {
				result as Int

				if ((c.required || hasKwargs) && result < 1) {
					throwInvalidValue(arguments, argument, parser, context)
				}

				if (hasKwargs) {
					if (result < kwArgs.size) {
						throwNotAllValid(arguments, argument, parser, context, kwArgs.size, result)
					}

					c.parseSuccess = true
				} else if (result > 0) {
					logger.trace { "Successfully filled argument: ${argument.displayName.key}" }

					c.parseSuccess = true
				}

				c.validate(context)
			}

			else -> {
				logger.error { "Converter with unknown base type: $c" }

				throw ArgumentParsingException(
					CoreTranslations.ArgumentParser.Error.errorInArgument
						.withContext(context)
						.withOrdinalPlaceholders(
							argument.displayName,

							CoreTranslations.ArgumentParser.Error.unknownConverterType
						),

					argument,
					arguments,
					parser
				)
			}
		}
	}

	/**
	 * Given a builder returning an [Arguments] subclass and [CommandContext], parse the command's arguments into
	 * the [Arguments] subclass and return it.
	 *
	 * This is a fairly complex function. It works like this:
	 *
	 * 1. Enumerate all of the converters provided by the [Arguments] subclass, mapping them to their display names
	 *    for easier keyword argument parsing.
	 * 2. Parse out the keyword arguments and store them in a map, leaving only the positional arguments in the list.
	 * 3. Loop over the converters, handing them the values they require.
	 *      * If a converter has keyword arguments, use those instead of taking a value from the list of arguments.
	 *      * For single, defaulting or optional converters, pass them a single argument and continue to the next.
	 *      * For coalescing or multi converters:
	 *          * If it's a keyword argument, pass it all of the keyed values and continue.
	 *          * If it's positional, pass it all remaining positional arguments and remove those that were converted.
	 *
	 * @param builder Builder returning an [Arguments] subclass - usually the constructor.
	 * @param context MessageCommand context for this command invocation.
	 *
	 * @return Built [Arguments] object, with converters filled.
	 * @throws DiscordRelayedException Thrown based on a lot of possible cases. This is intended for display on Discord.
	 */
	public open suspend fun <T : Arguments> parse(builder: () -> T, context: ChatCommandContext<*>): T {
		val argumentsObj = builder.invoke()
		argumentsObj.validate(context.getLocale())

		val parser = context.parser

		logger.trace { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

		val args = argumentsObj.args.toMutableList()
		val argsMap = args.associateBy { it.displayName.key.lowercase() }
		val keywordArgs: MutableStringKeyedMap<MutableList<String>> = mutableMapOf()

		if (context.chatCommand.allowKeywordArguments) {
			parser.parseNamed().forEach {
				val name = it.name.lowercase()

				keywordArgs[name] = keywordArgs[name] ?: mutableListOf()
				keywordArgs[name]!!.add(it.data)
			}

			logger.trace { "Parsed out ${keywordArgs.size} keyword args." }
		} else {
			logger.trace { "Skipping keyword args, command is configured to disallow them." }
		}

		logger.trace { "Args map: $argsMap" }

		var currentArg: Argument<*>?

		@Suppress("LoopWithTooManyJumpStatements")  // Listen here u lil shit
		while (true) {
			currentArg = args.removeFirstOrNull()
			currentArg ?: break  // If it's null, we're out of arguments

			val kwValue = keywordArgs[
				currentArg
					.getDefaultTranslatedDisplayName()
					.lowercase(context.getLocale())
			]

			val hasKwargs = kwValue != null

			logger.trace { "Current argument: ${currentArg!!.displayName}" }
			logger.trace { "Keyword arg ($hasKwargs): $kwValue" }

			if (!parser.cursor.hasNext && !hasKwargs) {
				continue
			}

			val parsed = doParse(argumentsObj, currentArg, parser, context, kwValue)

			if (parsed is Exception) {
				handleThrowable(argumentsObj, currentArg, parser, context, kwValue, parsed)
			}

			checkResult(argumentsObj, currentArg, parser, context, kwValue, parsed)
		}

		val allRequiredArgs = argsMap.count { it.value.converter.required }
		val filledRequiredArgs = argsMap.count { it.value.converter.parseSuccess && it.value.converter.required }

		logger.trace { "Filled $filledRequiredArgs / $allRequiredArgs arguments." }

		if (filledRequiredArgs < allRequiredArgs) {
			if (filledRequiredArgs < 1) {
				throw ArgumentParsingException(
					CoreTranslations.ArgumentParser.Error.noFilledArguments
						.withContext(context)
						.withOrdinalPlaceholders(allRequiredArgs),

					null,
					argumentsObj,
					parser
				)
			} else {
				throw ArgumentParsingException(
					CoreTranslations.ArgumentParser.Error.someFilledArguments
						.withContext(context)
						.withOrdinalPlaceholders(allRequiredArgs, filledRequiredArgs),

					null,
					argumentsObj,
					parser
				)
			}
		}

		return argumentsObj
	}

	/**
	 * Generate a command signature based on an [Arguments] subclass.
	 *
	 * @param builder Builder returning an [Arguments] subclass - usually the constructor.
	 * @return MessageCommand arguments signature for display.
	 */
	public open fun signature(builder: () -> Arguments, locale: Locale): String {
		val argumentsObj = builder.invoke()
		val parts = mutableListOf<String>()

		argumentsObj.args.forEach {
			val signature = buildString {
				if (it.converter.required) {
					append("<")
				} else {
					append("[")
				}

				append(
					it.displayName
						.withLocale(locale)
						.translate()
				)

				if (it.converter.showTypeInSignature) {
					append(": ")

					append(
						it.converter.signatureType
							.withLocale(locale)
							.translate()
					)

					if (it.converter is DefaultingConverter<*>) {
						append("=")
						append(it.converter.parsed)
					}
				}

				if (it.converter is ListConverter<*>) {
					append("...")
				}

				if (it.converter.required) {
					append(">")
				} else {
					append("]")
				}
			}

			parts.add(signature)
		}

		return parts.joinToString(" ")
	}
}
