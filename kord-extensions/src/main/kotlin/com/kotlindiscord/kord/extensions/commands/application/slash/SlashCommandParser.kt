@file:OptIn(KordPreview::class)
@file:Suppress(
    "StringLiteralDuplication" // Needs cleaning up with polymorphism later anyway
)

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.ArgumentParsingException
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.converters.*
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import mu.KotlinLogging

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
        context: SlashCommandContext<*, *>
    ): T {
        val argumentsObj = builder.invoke()
        argumentsObj.validate()

        logger.trace { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

        val args = argumentsObj.args.toMutableList()
        val command = context.event.interaction.command

        val values = command.options.mapValues {
            if (it.value is OptionValue.StringOptionValue) {
                OptionValue.StringOptionValue((it.value.value as String).trim())
            } else {
                it.value
            }
        } as Map<String, OptionValue<*>>

        var currentArg: Argument<*>?
        var currentValue: OptionValue<*>?

        @Suppress("LoopWithTooManyJumpStatements")  // Listen here u lil shit
        while (true) {
            currentArg = args.removeFirstOrNull()
            currentArg ?: break  // If it's null, we're out of arguments

            logger.trace { "Current argument: ${currentArg.displayName}" }

            currentValue = values[currentArg.displayName.lowercase()]

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
                            context.translate(
                                "argumentParser.error.invalidValue",
                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            ),

                            "argumentParser.error.invalidValue",

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
                            null,

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
                            context.translate(
                                "argumentParser.error.invalidValue",
                                replacements = arrayOf(
                                    currentArg.displayName,
                                    converter.getErrorString(context),
                                    currentValue
                                )
                            ),

                            "argumentParser.error.invalidValue",

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
                            null,

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

                        converter.validate(context)
                    }
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError) {
                        throw ArgumentParsingException(
                            converter.handleError(e, context),
                            null,

                            currentArg,
                            argumentsObj,
                            null
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
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

                        converter.validate(context)
                    }
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError) {
                        throw ArgumentParsingException(
                            converter.handleError(e, context),
                            null,

                            currentArg,
                            argumentsObj,
                            null
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
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

                        converter.validate(context)
                    }
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError) {
                        throw ArgumentParsingException(
                            converter.handleError(e, context),
                            null,

                            currentArg,
                            argumentsObj,
                            null
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
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

                        converter.validate(context)
                    }
                } catch (e: DiscordRelayedException) {
                    if (converter.required || converter.outputError) {
                        throw ArgumentParsingException(
                            converter.handleError(e, context),
                            null,

                            currentArg,
                            argumentsObj,
                            null
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
                }

                else -> error("Unsupported type for converter: $converter")
            }
        }

        return argumentsObj
    }
}
