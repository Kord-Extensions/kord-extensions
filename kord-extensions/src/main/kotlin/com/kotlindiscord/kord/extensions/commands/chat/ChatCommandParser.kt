/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress(
    "TooGenericExceptionCaught",
    "StringLiteralDuplication",
    "DuplicatedCode"
)

package com.kotlindiscord.kord.extensions.commands.chat

import com.kotlindiscord.kord.extensions.ArgumentParsingException
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.getDefaultTranslatedDisplayName
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
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
     * @throws DiscordRelayedException Thrown based on a lot of possible cases. This is intended for display on Discord.
     */
    public open suspend fun <T : Arguments> parse(builder: () -> T, context: ChatCommandContext<*>): T {
        val argumentsObj = builder.invoke()
        argumentsObj.validate()

        val parser = context.parser

        logger.trace { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

        val args = argumentsObj.args.toMutableList()
        val argsMap = args.associateBy { it.displayName.lowercase() }
        val keywordArgs: MutableMap<String, MutableList<String>> = mutableMapOf()

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
                    .getDefaultTranslatedDisplayName(context.translationsProvider, context.command)
                    .lowercase(context.getLocale())
            ]

            val hasKwargs = kwValue != null

            logger.trace { "Current argument: ${currentArg.displayName}" }
            logger.trace { "Keyword arg ($hasKwargs): $kwValue" }

            if (!parser.cursor.hasNext && !hasKwargs) {
                continue
            }

            when (val converter = currentArg.converter) {
                is ChoiceConverter<*> -> error("Choice converters may only be used with slash commands")

                is SingleConverter<*> -> try {
                    val parsed = if (hasKwargs) {
                        if (kwValue!!.size != 1) {
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.requiresOneValue",
                                    replacements = arrayOf(currentArg.displayName, kwValue.size)
                                ),

                                "argumentParser.error.requiresOneValue",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parse(parser, context, kwValue.first())
                    } else {
                        converter.parse(parser, context)
                    }

                    if ((converter.required || hasKwargs) && !parsed) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.getErrorString(context),
                                )
                            ),

                            "argumentParser.error.invalidValue",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }

                    if (parsed) {
                        logger.trace { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true

                        converter.validate(context)
                    }
                } catch (e: DiscordRelayedException) {
                    if (converter.required || hasKwargs) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
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
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.requiresOneValue",
                                    replacements = arrayOf(currentArg.displayName, kwValue.size)
                                ),

                                "argumentParser.error.requiresOneValue",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parse(parser, context, kwValue.first())
                    } else {
                        converter.parse(parser, context)
                    }

                    if (parsed) {
                        logger.trace { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                    }

                    converter.validate(context)
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
                }

                is OptionalConverter<*> -> try {
                    val parsed = if (hasKwargs) {
                        if (kwValue!!.size != 1) {
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.requiresOneValue",
                                    replacements = arrayOf(currentArg.displayName, kwValue.size)
                                ),

                                "argumentParser.error.requiresOneValue",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parse(parser, context, kwValue.first())
                    } else {
                        converter.parse(parser, context)
                    }

                    if (parsed) {
                        logger.trace { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                    }

                    converter.validate(context)
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required || hasKwargs) {
                        throw t
                    }
                }

                is ListConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(parser, context, kwValue!!)
                    } else {
                        converter.parse(parser, context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.getErrorString(context)
                                )
                            ),

                            "argumentParser.error.invalidValue",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        context.translate(
                                            currentArg.displayName,
                                            bundleName = converter.bundle
                                        ),

                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString, bundleName = converter.bundle)
                                    )
                                ),

                                "argumentParser.error.notAllValid",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parseSuccess = true
                    } else {
                        if (parsedCount > 0) {
                            logger.trace { "Argument ${currentArg.displayName} successfully filled." }

                            converter.parseSuccess = true
                        }
                    }

                    converter.validate(context)
                } catch (e: DiscordRelayedException) {
                    if (converter.required) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
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
                        converter.parse(parser, context, kwValue!!)
                    } else {
                        converter.parse(parser, context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.getErrorString(context),
                                )
                            ),

                            "argumentParser.error.invalidValue",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        context.translate(
                                            currentArg.displayName,
                                            bundleName = converter.bundle
                                        ),

                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString, bundleName = converter.bundle)
                                    )
                                ),

                                "argumentParser.error.notAllValid",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parseSuccess = true

                        converter.validate(context)
                    } else {
                        if (parsedCount > 0) {
                            logger.trace { "Argument '${currentArg.displayName}' successfully filled." }

                            converter.parseSuccess = true

                            converter.validate(context)
                        }
                    }
                } catch (e: DiscordRelayedException) {
                    if (converter.required) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
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
                        converter.parse(parser, context, kwValue!!)
                    } else {
                        converter.parse(parser, context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.getErrorString(context),
                                )
                            ),

                            "argumentParser.error.invalidValue",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        context.translate(
                                            currentArg.displayName,
                                            bundleName = converter.bundle
                                        ),

                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString, bundleName = converter.bundle)
                                    )
                                ),

                                "argumentParser.error.notAllValid",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parseSuccess = true
                    } else {
                        if (parsedCount > 0) {
                            logger.trace { "Argument '${currentArg.displayName}' successfully filled." }

                            converter.parseSuccess = true
                        }
                    }

                    converter.validate(context)
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required || converter.outputError || hasKwargs) {
                        throw t
                    }
                }

                is DefaultingCoalescingConverter<*> -> try {
                    val parsedCount = if (hasKwargs) {
                        converter.parse(parser, context, kwValue!!)
                    } else {
                        converter.parse(parser, context)
                    }

                    if ((converter.required || hasKwargs) && parsedCount <= 0) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.invalidValue",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.getErrorString(context),
                                )
                            ),

                            "argumentParser.error.invalidValue",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }

                    if (hasKwargs) {
                        if (parsedCount < kwValue!!.size) {
                            throw ArgumentParsingException(
                                context.translate(
                                    "argumentParser.error.notAllValid",

                                    replacements = arrayOf(
                                        context.translate(
                                            currentArg.displayName,
                                            bundleName = context.command.resolvedBundle ?: converter.bundle
                                        ),

                                        kwValue.size,
                                        parsedCount,
                                        context.translate(converter.signatureTypeString, bundleName = converter.bundle)
                                    )
                                ),

                                "argumentParser.error.notAllValid",

                                currentArg,
                                argumentsObj,
                                parser
                            )
                        }

                        converter.parseSuccess = true
                    } else {
                        if (parsedCount > 0) {
                            logger.trace { "Argument '${currentArg.displayName}' successfully filled." }

                            converter.parseSuccess = true
                        }
                    }

                    converter.validate(context)
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError || hasKwargs) {
                        throw ArgumentParsingException(
                            context.translate(
                                "argumentParser.error.errorInArgument",

                                replacements = arrayOf(
                                    context.translate(
                                        currentArg.displayName,
                                        bundleName = context.command.resolvedBundle ?: converter.bundle
                                    ),

                                    converter.handleError(e, context)
                                )
                            ),

                            "argumentParser.error.errorInArgument",

                            currentArg,
                            argumentsObj,
                            parser
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required || converter.outputError) {
                        throw t
                    }
                }

                else -> throw ArgumentParsingException(
                    context.translate(
                        "argumentParser.error.errorInArgument",

                        replacements = arrayOf(
                            context.translate(
                                currentArg.displayName,
                                bundleName = context.command.resolvedBundle ?: converter.bundle
                            ),

                            context.translate(
                                "argumentParser.error.unknownConverterType",
                                replacements = arrayOf(currentArg.converter)
                            )
                        )
                    ),

                    "argumentParser.error.errorInArgument",

                    currentArg,
                    argumentsObj,
                    parser
                )
            }
        }

        val allRequiredArgs = argsMap.count { it.value.converter.required }
        val filledRequiredArgs = argsMap.count { it.value.converter.parseSuccess && it.value.converter.required }

        logger.trace { "Filled $filledRequiredArgs / $allRequiredArgs arguments." }

        if (filledRequiredArgs < allRequiredArgs) {
            if (filledRequiredArgs < 1) {
                throw ArgumentParsingException(
                    context.translate(
                        "argumentParser.error.noFilledArguments",

                        replacements = arrayOf(
                            allRequiredArgs
                        )
                    ),

                    "argumentParser.error.noFilledArguments",

                    null,
                    argumentsObj,
                    parser
                )
            } else {
                throw ArgumentParsingException(
                    context.translate(
                        "argumentParser.error.someFilledArguments",

                        replacements = arrayOf(
                            allRequiredArgs,
                            filledRequiredArgs
                        )
                    ),

                    "argumentParser.error.someFilledArguments",

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

                append(it.displayName)

                if (it.converter.showTypeInSignature) {
                    append(": ")

                    append(
                        translationsProvider.translate(
                            it.converter.signatureTypeString,
                            it.converter.bundle,
                            locale
                        )
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
