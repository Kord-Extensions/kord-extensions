@file:Suppress(
    "StringLiteralDuplication" // Needs cleaning up with polymorphism later anyway
)

package com.kotlindiscord.kord.extensions.commands.slash.parser

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.CoalescingConverter
import com.kotlindiscord.kord.extensions.commands.converters.DefaultingConverter
import com.kotlindiscord.kord.extensions.commands.converters.OptionalConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.ArgumentParser
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Parser in charge of dealing with the arguments for slash commands.
 *
 * This doesn't do anything special with the rich types provided by Discord, as they're useless in most cases.
 * Instead, it transforms them to a string and puts them through the usual parsing machinery.
 *
 * This parser does not support multi converters, as there's no good way to represent them with
 * Discord's API. Coalescing converters will act like single converters.
 */
public open class SlashCommandParser(bot: ExtensibleBot) : ArgumentParser(bot) {
    public override suspend fun <T : Arguments> parse(builder: () -> T, context: CommandContext): T {
        if (context !is SlashCommandContext<out Arguments>) {
            error("This parser only supports slash commands.")
        }

        val argumentsObj = builder.invoke()

        logger.debug { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

        val args = argumentsObj.args.toMutableList()
        val command = context.event.interaction.command
        val options = command.options

//        val options = when {
//            command.subCommands.isNotEmpty() -> command.subCommands.values.first().options
//            command.groups.isNotEmpty() -> command.groups.values.first().subCommands.values.first().options
//
//            else -> command.options
//        }

        val values = options.mapValues { it.value.value!!.toString() }

        var currentArg: Argument<*>?
        var currentValue: String? = null

        @Suppress("LoopWithTooManyJumpStatements")  // Listen here u lil shit
        while (true) {
            currentArg = args.removeFirstOrNull()
            currentArg ?: break  // If it's null, we're out of arguments

            logger.debug { "Current argument: ${currentArg.displayName}" }

            currentValue = values[currentArg.displayName]

            logger.debug { "Current value: $currentValue" }

            @Suppress("TooGenericExceptionCaught")
            when (val converter = currentArg.converter) {
                // It's worth noting that Discord handles validation for required converters, so we don't need to
                // do that checking ourselves, really

                is SingleConverter<*> -> try {
                    val parsed = if (currentValue != null) {
                        converter.parse(currentValue, context, bot)
                    } else {
                        false
                    }

                    if (converter.required && !parsed) {
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
                    if (converter.required) {
                        throw ParseException(converter.handleError(e, currentValue, context, bot))
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is CoalescingConverter<*> -> try {
                    val parsed = if (currentValue != null) {
                        converter.parse(listOf(currentValue), context, bot) > 0
                    } else {
                        false
                    }

                    if (converter.required && !parsed) {
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
                    if (converter.required) {
                        val wrappedValues = mutableListOf<String>()

                        if (currentValue != null) {
                            wrappedValues += currentValue
                        }

                        throw ParseException(converter.handleError(e, wrappedValues, context, bot))
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                    if (converter.required) {
                        throw t
                    }
                }

                is OptionalConverter<*> -> try {
                    val parsed = if (currentValue != null) {
                        converter.parse(currentValue, context, bot)
                    } else {
                        false
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null
                    }
                } catch (e: ParseException) {
                    if (converter.required || converter.outputError) {
                        throw ParseException(
                            converter.handleError(e, currentValue, context, bot)
                        )
                    }
                } catch (t: Throwable) {
                    logger.debug { "Argument ${currentArg.displayName} threw: $t" }
                }

                is DefaultingConverter<*> -> try {
                    val parsed = if (currentValue != null) {
                        converter.parse(currentValue, context, bot)
                    } else {
                        false
                    }

                    if (parsed) {
                        logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                        converter.parseSuccess = true
                        currentValue = null
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
