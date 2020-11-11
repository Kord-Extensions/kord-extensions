package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import io.ktor.client.features.*

open class NewArgumentParser(private val bot: ExtensibleBot, private val splitChar: Char = '=') {
    suspend fun <T : Arguments> parse(builder: () -> T, context: CommandContext): T {
        val argumentsObj = builder.invoke()

        val args = argumentsObj.args.toMutableList()
        val argsMap = args.map { Pair(it.displayName, it.converter) }.toMap()
        val keywordArgs = mutableMapOf<String, MutableList<String>>()

        var currentArg: Argument<*>? = null
        var currentValue: String? = null

        val values = context.args.filter { v ->
            if (v.contains(splitChar)) {
                val split = v.split(splitChar, limit = 1)

                val key = split.first().toLowerCase()
                val value = split.last()

                val converter = argsMap[key]

                if (converter != null && converter is MultiConverter<*>) {
                    keywordArgs[key] = keywordArgs[key] ?: mutableListOf()
                    keywordArgs[key]!!.add(value)

                    return@filter false
                }
            }

            return@filter true
        }.toMutableList()

        for ((key, value) in keywordArgs.entries) {
            val converter = argsMap[key] as MultiConverter<*> ?: error("Converter disappeared: $key")

            val parsedCount = converter.parse(value.toList(), context, bot)

            if (converter.required && parsedCount <= 0) {
                throw ParseException("Invalid value/s for argument $key: ${value.joinToString(" ")}")
            }

            if (converter.required && parsedCount < value.size) {
                throw ParseException(
                    "Argument $key was provided with ${value.size} values, but only $parsedCount were valid."
                )
            }
        }

        while (true) {
            currentArg = args.removeFirstOrNull()
            currentArg ?: break  // If it's null, we're out of arguments

            // If this is the case, the converter has been given a keyword arg and should be skipped
            if (keywordArgs[currentArg.displayName] != null) continue

            currentValue = currentValue ?: values.removeFirstOrNull()
            currentValue ?: break  // If it's null, we're out of values

            val converter = currentArg.converter

            when (converter) {
                is SingleConverter<*> -> {
                    try {
                        val parsed = converter.parse(currentValue, context, bot)

                        if (converter.required && !parsed) {
                            throw ParseException("Invalid value for argument ${currentArg.displayName}: $currentValue")
                        }

                        if (parsed) {
                            currentValue = null
                            converter.parseSuccess = true
                        }
                    } catch (t: Throwable) {
                        if (converter.required) {
                            throw ParseException(converter.handleError(t))
                        }
                    }
                }
                is MultiConverter<*> -> {
                    try {
                        val parsedCount = converter.parse(values.toList(), context, bot)

                        if (converter.required && parsedCount <= 0) {
                            throw ParseException("Invalid value for argument ${currentArg.displayName}: $currentValue")
                        }

                        if (parsedCount > 0) {
                            currentValue = null
                            converter.parseSuccess = true
                        }

                        (0 until parsedCount).forEach { _ -> values.removeFirst() }
                    } catch (t: Throwable) {
                        if (converter.required) {
                            throw ParseException(converter.handleError(t))
                        }
                    }
                }
                else -> {
                    throw ParseException("Unknown converter type provided: ${currentArg.converter}")
                }
            }
        }

        val allRequiredArgs = args.count { it.converter.required }
        val filledRequiredArgs = args.count { it.converter.parseSuccess && it.converter.required }

        if (filledRequiredArgs < allRequiredArgs) {
            throw ParseException(
                "This command has $allRequiredArgs required arguments, but only $filledRequiredArgs could be filled."
            )
        }

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
