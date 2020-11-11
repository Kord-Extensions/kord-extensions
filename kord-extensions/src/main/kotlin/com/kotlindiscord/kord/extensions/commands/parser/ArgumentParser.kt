package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import io.ktor.client.features.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

open class ArgumentParser(private val bot: ExtensibleBot, private val splitChar: Char = '=') {
    suspend fun <T : Arguments> parse(builder: () -> T, context: CommandContext): T {
        val argumentsObj = builder.invoke()

        logger.debug { "Arguments object: $argumentsObj (${argumentsObj.args.size} args)" }

        val args = argumentsObj.args.toMutableList()
        val argsMap = args.map { Pair(it.displayName.toLowerCase(), it) }.toMap()
        val keywordArgs = mutableMapOf<String, MutableList<String>>()

        logger.debug { "Args map: $argsMap" }

        var currentArg: Argument<*>? = null
        var currentValue: String? = null

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

                    return@filter false
                } else {
                    logger.debug { "Invalid argument: $argument" }
                }
            }

            return@filter true
        }.toMutableList()

        for ((key, value) in keywordArgs.entries) {
            val argument = argsMap[key] ?: error("Converter disappeared: $key")
            val argName = argument.displayName

            try {
                if (argument.converter is SingleConverter<*>) {
                    if (value.size != 1) {
                        throw ParseException("Argument $argName requires exactly 1 argument, but ${value.size} were provided.")
                    }

                    val singleValue = value.first()

                    val parsed = argument.converter.parse(singleValue, context, bot)

                    if (argument.converter.required && !parsed) {
                        throw ParseException("Invalid value for argument $argName: $singleValue")
                    }

                    if (parsed) {
                        logger.debug { "Argument $argName successfully filled." }

                        argument.converter.parseSuccess = true
                    } else {
                        throw ParseException(
                            "Invalid value for argument $argName: $singleValue"
                        )
                    }
                } else if (argument.converter is MultiConverter<*>) {
                    val parsedCount = argument.converter.parse(value.toList(), context, bot)

                    if (argument.converter.required && parsedCount <= 0) {
                        throw ParseException("Invalid value/s for argument $argName: ${value.joinToString(" ")}")
                    }

                    if (argument.converter.required && parsedCount < value.size) {
                        throw ParseException(
                            "Argument $argName was provided with ${value.size} values, but only $parsedCount were valid."
                        )
                    }

                    argument.converter.parseSuccess = true
                } else {
                    throw ParseException("Unknown converter type provided: ${argument.converter}")
                }
            } catch (t: Throwable) {
                logger.debug { "Argument $argName threw: $t" }

                if (t !is ParseException) {
                    throw ParseException(argument.converter.handleError(t))
                }

                throw t
            }
        }

        while (true) {
            currentArg = args.removeFirstOrNull()
            currentArg ?: break  // If it's null, we're out of arguments

            logger.debug { "Current argument: ${currentArg.displayName}" }

            // If this is the case, the converter has been given a keyword arg and should be skipped
            if (keywordArgs[currentArg.displayName] != null) continue

            currentValue = currentValue ?: values.removeFirstOrNull()
            currentValue ?: break  // If it's null, we're out of values

            logger.debug { "Current value: ${currentArg.displayName}" }

            val converter = currentArg.converter

            when (converter) {
                is SingleConverter<*> -> {
                    try {
                        val parsed = converter.parse(currentValue, context, bot)

                        if (converter.required && !parsed) {
                            throw ParseException("Invalid value for argument ${currentArg.displayName}: $currentValue")
                        }

                        if (parsed) {
                            logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true
                        }
                    } catch (t: Throwable) {
                        logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                        if (converter.required) {
                            if (t !is ParseException) {
                                throw ParseException(converter.handleError(t))
                            }

                            throw t
                        }
                    }
                }
                is MultiConverter<*> -> {
                    try {
                        val parsedCount = converter.parse(listOf(currentValue) + values.toList(), context, bot) - 1

                        if (converter.required && parsedCount <= 0) {
                            throw ParseException("Invalid value for argument ${currentArg.displayName}: $currentValue")
                        }

                        if (parsedCount > 0) {
                            logger.debug { "Argument ${currentArg.displayName} successfully filled." }

                            currentValue = null
                            converter.parseSuccess = true
                        }

                        (0 until parsedCount).forEach { _ -> values.removeFirst() }
                    } catch (t: Throwable) {
                        logger.debug { "Argument ${currentArg.displayName} threw: $t" }

                        if (converter.required) {
                            if (t !is ParseException) {
                                throw ParseException(converter.handleError(t))
                            }

                            throw t
                        }
                    }
                }
                else -> {
                    throw ParseException("Unknown converter type provided: ${currentArg.converter}")
                }
            }
        }

        val allRequiredArgs = argsMap.count { it.value.converter.required }
        val filledRequiredArgs = argsMap.count { it.value.converter.parseSuccess && it.value.converter.required }

        logger.debug { "Filled $filledRequiredArgs / $allRequiredArgs args." }

        if (filledRequiredArgs < allRequiredArgs) {
            throw ParseException(
                "This command has $allRequiredArgs required arguments, but only $filledRequiredArgs could be filled."
            )
        }

        logger.debug { "Leftover arguments: ${values.size}" }

        return argumentsObj
    }

    fun signature(builder: () -> Arguments): String {
        val argumentsObj = builder.invoke()
        val parts = mutableListOf<String>()

        argumentsObj.args.forEach {
            var signature = ""

            signature += if (it.converter.required) {
                "<"
            } else {
                "["
            }

            signature += "${it.displayName}: ${it.converter.typeString}"

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
