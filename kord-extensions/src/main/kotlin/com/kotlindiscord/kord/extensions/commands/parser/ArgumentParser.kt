@file:Suppress(
    "TooGenericExceptionCaught",
    "StringLiteralDuplication" // Needs cleaning up with polymorphism later anyway
)

package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import io.ktor.client.features.*
import mu.KotlinLogging

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
 * @param bot Current instance of the bot
 * @param splitChar The character to use for splitting keyword arguments
 */
public open class ArgumentParser(public val bot: ExtensibleBot, private val splitChar: Char = '=') {
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
     * @throws ParseException Thrown based on a lot of possible cases. This is intended for display on Discord.
     */
    public open suspend fun <T : Arguments> parse(builder: () -> T, context: CommandContext): T {
        val argumentsObj = builder.invoke()

        logger.debug { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

        val args = argumentsObj.args.toMutableList()
        val argsMap = args.map { Pair(it.displayName.toLowerCase(), it) }.toMap()
        val keywordArgs = mutableMapOf<String, MutableList<String>>()

        logger.debug { "Args map: $argsMap" }

        val values = context.args.filter { v ->
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
                            throw ParseException(
                                "Argument `${currentArg.displayName}` requires exactly 1 value, but " +
                                    "${kwValue.size} were provided."
                            )
                        }

                        converter.parse(kwValue.first(), context, bot)
                    } else {
                        converter.parse(currentValue, context, bot)
                    }

                    if ((converter.required || hasKwargs) && !parsed) {
                        throw ParseException(
                            "Invalid value for argument `${currentArg.displayName}` " +
                                "(which accepts ${converter.getErrorString()}): $currentValue"
                        )
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null
                    }
                } catch (e: ParseException) {
                    if (converter.required || hasKwargs) {
                        throw ParseException(
                            converter.handleError(e, currentValue, context, bot)
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
                            throw ParseException(
                                "Argument `${currentArg.displayName}` requires exactly 1 value, but " +
                                    "${kwValue.size} were provided."
                            )
                        }

                        converter.parse(kwValue.first(), context, bot)
                    } else {
                        converter.parse(currentValue, context, bot)
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
                }

                is OptionalConverter<*> -> try {
                    val parsed = if (hasKwargs) {
                        if (kwValue!!.size != 1) {
                            throw ParseException(
                                "Argument `${currentArg.displayName}` requires exactly 1 value, but " +
                                    "${kwValue.size} were provided."
                            )
                        }

                        converter.parse(kwValue.first(), context, bot)
                    } else {
                        converter.parse(currentValue, context, bot)
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null
                    }
                } catch (e: ParseException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw ParseException(
                            converter.handleError(e, currentValue, context, bot)
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
                        converter.parse(kwValue!!, context, bot)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context, bot)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ParseException(
                            "Invalid value for argument `${currentArg.displayName}` (which accepts " +
                                "${converter.getErrorString()}): $currentValue"
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ParseException(
                                "Argument `${currentArg.displayName}` was provided with ${kwValue.size} " +
                                    "value${if (kwValue.size > 1) "d" else ""}, but " +
                                    if (parsedCount >= 1) {
                                        "only $parsedCount of them were valid ${converter.signatureTypeString}."
                                    } else {
                                        "none were valid ${converter.signatureTypeString}."
                                    }
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: ParseException) {
                    if (converter.required) throw ParseException(converter.handleError(e, values, context, bot))
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is CoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context, bot)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context, bot)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ParseException(
                            "Invalid value for argument `${currentArg.displayName}` (which accepts " +
                                "${converter.getErrorString()}): $currentValue"
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ParseException(
                                "Argument `${currentArg.displayName}` was provided with ${kwValue.size} " +
                                    "value${if (kwValue.size > 1) "d" else ""}, but " +
                                    if (parsedCount >= 1) {
                                        "only $parsedCount of them were valid ${converter.signatureTypeString}."
                                    } else {
                                        "none were valid ${converter.signatureTypeString}."
                                    }
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument '${currentArg.displayName}' successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: ParseException) {
                    if (converter.required) throw ParseException(converter.handleError(e, values, context, bot))
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is OptionalCoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context, bot)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context, bot)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ParseException(
                            "Invalid value for argument `${currentArg.displayName}` (which accepts " +
                                "${converter.getErrorString()}): $currentValue"
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ParseException(
                                "Argument `${currentArg.displayName}` was provided with ${kwValue.size} " +
                                    "value${if (kwValue.size > 1) "d" else ""}, but " +
                                    if (parsedCount >= 1) {
                                        "only $parsedCount of them were valid ${converter.signatureTypeString}."
                                    } else {
                                        "none were valid ${converter.signatureTypeString}."
                                    }
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument '${currentArg.displayName}' successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: ParseException) {
                    if (converter.required) throw ParseException(converter.handleError(e, values, context, bot))
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is DefaultingCoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(kwValue!!, context, bot)
                    } else {
                        converter.parse(listOf(currentValue) + values.toList(), context, bot)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ParseException(
                            "Invalid value for argument `${currentArg.displayName}` (which accepts " +
                                "${converter.getErrorString()}): $currentValue"
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ParseException(
                                "Argument `${currentArg.displayName}` was provided with ${kwValue.size} " +
                                    "value${if (kwValue.size > 1) "d" else ""}, but " +
                                    if (parsedCount >= 1) {
                                        "only $parsedCount of them were valid ${converter.signatureTypeString}."
                                    } else {
                                        "none were valid ${converter.signatureTypeString}."
                                    }
                            )
                        }

                        converter.parseSuccess = true
                        currentValue = null
                    } else {
                        if (parsedCount > 0) {
                            logger.debug { "Argument '${currentArg.displayName}' successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true
                        }

                        (0 until parsedCount - 1).forEach { _ -> values.removeFirst() }
                    }
                } catch (e: ParseException) {
                    if (converter.required) throw ParseException(converter.handleError(e, values, context, bot))
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                else -> throw ParseException("Unknown converter type provided: ${currentArg.converter}")
            }
        }

        val allRequiredArgs = argsMap.count { it.value.converter.required }
        val filledRequiredArgs = argsMap.count { it.value.converter.parseSuccess && it.value.converter.required }

        logger.debug { "Filled $filledRequiredArgs / $allRequiredArgs arguments." }

        if (filledRequiredArgs < allRequiredArgs) {
            if (filledRequiredArgs < 1) {
                throw ParseException(
                    "This command has $allRequiredArgs required argument${if (allRequiredArgs > 1) "s" else ""}."
                )
            } else {
                throw ParseException(
                    "This command has $allRequiredArgs required argument${if (allRequiredArgs > 1) "s" else ""}, " +
                        "but only $filledRequiredArgs could be filled."
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
    public open fun signature(builder: () -> Arguments): String {
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
                signature += ": ${it.converter.signatureTypeString}"

                if (it.converter is DefaultingConverter<*>) {
                    signature += "=${it.converter.parsed}"
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
