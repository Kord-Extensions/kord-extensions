@file:OptIn(KordPreview::class)

@file:Suppress(
    "TooGenericExceptionCaught",
    "StringLiteralDuplication" // Needs cleaning up with polymorphism later anyway
)

package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import dev.kord.common.annotation.KordPreview
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
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
 *
 * @param splitChar The character to use for splitting keyword arguments
 */
public open class ArgumentParser(private val splitChar: Char = '=') : KoinComponent {
    /** Current instance of the bot. **/
    public open val bot: ExtensibleBot by inject()

    /** Translations provider, for retrieving translations. **/
    public val translationsProvider: TranslationsProvider by inject()

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
     * @throws CommandException Thrown based on a lot of possible cases. This is intended for display on Discord.
     */
    public open suspend fun <T : Arguments> parse(builder: () -> T, context: CommandContext): T {
        val argumentsObj = builder.invoke()

        logger.debug { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

        val args = argumentsObj.args.toMutableList()
        val argsMap = args.map { Pair(it.displayName.toLowerCase(), it) }.toMap()
        val keywordArgs = mutableMapOf<String, MutableList<String>>()

        logger.debug { "Args map: $argsMap" }

        val values = context.argsList.filter { v ->
            if (v.contains(splitChar)) {
                logger.debug { "Potential keyword argument: $v" }

                val split = v.split(splitChar, limit = 2)
                val key = split.first().toLowerCase()
                val value = split.last()

                logger.debug { "Split value: $key -> $value" }

                val argument = argsMap[key]

                if (argument != null) {
                    logger.debug { "Found valid argument: $argument" }

                    keywordArgs[key] = keywordArgs[key] ?: mutableListOf()
                    keywordArgs[key]!!.add(value)

                    return@filter false  // This is a keyword argument, filter it from the rest
                } else {
                    logger.debug { "Invalid argument: $key" }
                }
            }

            return@filter true
        }.toMutableList()

        var currentArg: Argument<*>?
        var currentValue: String? = null

        @Suppress("LoopWithTooManyJumpStatements")  // Listen here u lil shit
        while (true) {
            currentArg = args.removeFirstOrNull()
            currentArg ?: break  // If it's null, we're out of arguments

            val kwValue = keywordArgs[currentArg.displayName.toLowerCase()]
            val hasKwargs = kwValue != null

            logger.debug { "Current argument: ${currentArg.displayName}" }
            logger.debug { "Keyword arg ($hasKwargs): $kwValue" }

            currentValue = if (!hasKwargs) {  // Keyword args get values elsewhere
                currentValue ?: values.removeFirstOrNull()
            } else {
                currentValue ?: ""  // To avoid skipping
            }

            currentValue ?: continue  // If it's null, we're out of values

            logger.debug { "Current value: $currentValue" }

            when (val converter = currentArg.converter) {
                is SingleConverter<*> -> try {
                    val parsed = if (hasKwargs) {
                        if (kwValue!!.size != 1) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.requiresOneValue",
                                    replacements = arrayOf(currentArg.displayName, kwValue.size)
                                )
                            )
                        }

                        converter.parse(kwValue.first(), context)
                    } else {
                        converter.parse(currentValue, context)
                    }

                    if ((converter.required || hasKwargs) && !parsed) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            )
                        )
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null

                        converter.validate()
                    }
                } catch (e: CommandException) {
                    if (converter.required || hasKwargs) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    currentArg.displayName,

                                    converter.handleError(e, currentValue, context)
                                )
                            )
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required || hasKwargs) {
                        throw t
                    }
                }

                is DefaultingConverter<*> -> try {
                    val parsed = if (hasKwargs) {
                        if (kwValue!!.size != 1) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.requiresOneValue",
                                    replacements = arrayOf(currentArg.displayName, kwValue.size)
                                )
                            )
                        }

                        converter.parse(kwValue.first(), context)
                    } else {
                        converter.parse(currentValue, context)
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null

                        converter.validate()
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
                }

                is OptionalConverter<*> -> try {
                    val parsed = if (hasKwargs) {
                        if (kwValue!!.size != 1) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.requiresOneValue",
                                    replacements = arrayOf(currentArg.displayName, kwValue.size)
                                )
                            )
                        }

                        converter.parse(kwValue.first(), context)
                    } else {
                        converter.parse(currentValue, context)
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null

                        converter.validate()
                    }
                } catch (e: CommandException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    currentArg.displayName,

                                    converter.handleError(e, currentValue, context)
                                )
                            )
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required || hasKwargs) {
                        throw t
                    }
                }

                is MultiConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            )
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        currentArg.displayName,
                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString)
                                    )
                                )
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true

                            converter.validate()
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: CommandException) {
                    if (converter.required) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    currentArg.displayName,

                                    converter.handleError(e, values, context)
                                )
                            )
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is CoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            )
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        currentArg.displayName,
                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString)
                                    )
                                )
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument '${currentArg.displayName}' successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true

                            converter.validate()
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: CommandException) {
                    if (converter.required) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    currentArg.displayName,

                                    converter.handleError(e, values, context)
                                )
                            )
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is OptionalCoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            )
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        currentArg.displayName,
                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString)
                                    )
                                )
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument '${currentArg.displayName}' successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true

                            converter.validate()
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: CommandException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    currentArg.displayName,

                                    converter.handleError(e, values, context)
                                )
                            )
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required || hasKwargs) {
                        throw t
                    }
                }

                is DefaultingCoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            )
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw CommandException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        currentArg.displayName,
                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString)
                                    )
                                )
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument '${currentArg.displayName}' successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true

                            converter.validate()
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: CommandException) {
                    if (converter.required) {
                        throw CommandException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    currentArg.displayName,

                                    converter.handleError(e, values, context)
                                )
                            )
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                else -> throw CommandException(
                    context.translate(
                        "argumentParser.error.errorInArgument",

                        replacements = arrayOf(
                            currentArg.displayName,

                            context.translate(
                                "argumentParser.error.unknownConverterType",
                                replacements = arrayOf(currentArg.converter)
                            )
                        )
                    )
                )
            }
        }

        val allRequiredArgs = argsMap.count { it.value.converter.required }
        val filledRequiredArgs = argsMap.count { it.value.converter.parseSuccess && it.value.converter.required }

        logger.debug { "Filled $filledRequiredArgs / $allRequiredArgs arguments." }

        if (filledRequiredArgs < allRequiredArgs) {
            if (filledRequiredArgs < 1) {
                throw CommandException(
                    context.translate(
                        "argumentParser.error.noFilledArguments",

                        replacements = arrayOf(
                            allRequiredArgs
                        )
                    )
                )
            } else {
                throw CommandException(
                    context.translate(
                        "argumentParser.error.someFilledArguments",

                        replacements = arrayOf(
                            allRequiredArgs,
                            filledRequiredArgs
                        )
                    )
                )
            }
        }

        logger.debug { "Leftover arguments: ${values.size}" }

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
            var signature = ""

            signature += if (it.converter.required) {
                "<"
            } else {
                "["
            }

            signature += it.displayName

            if (it.converter.showTypeInSignature) {
                signature += ": "

                signature += translationsProvider.translate(
                    it.converter.signatureTypeString,
                    it.converter.bundle,
                    locale
                )

                if (it.converter is DefaultingConverter<*>) {
                    signature += "="
                    signature += it.converter.parsed
                }
            }

            if (it.converter is MultiConverter<*>) {
                signature += "..."
            }

            signature += if (it.converter.required) {
                ">"
            } else {
                "]"
            }

            parts.add(signature)
        }

        return parts.joinToString(" ")
    }
}
