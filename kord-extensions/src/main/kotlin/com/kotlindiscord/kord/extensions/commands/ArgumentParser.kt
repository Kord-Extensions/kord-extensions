package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Guild
import com.gitlab.kordlib.core.entity.GuildEmoji
import com.gitlab.kordlib.core.entity.Role
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.event.message.MessageCreateEvent
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import mu.KotlinLogging
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

private val logger = KotlinLogging.logger {}

/**
 * Class in charge of converting string arguments for commands into fully-typed data classes.
 *
 * You most likely don't need to touch this yourself - it's part of the commands framework.
 *
 * @param bot Instance of [ExtensibleBot] we're working with.
 */
class ArgumentParser(private val bot: ExtensibleBot) {
    /** Defined here so we don't have to create it every time we try to parse something. */
    private val listType = List::class.createType(arguments = listOf(KTypeProjection.STAR))

    private val mentionRegex = Regex("^<(?:@[!&]?|#)(\\d+)>$")

    /**
     * Given a data class and an array of strings, return an instance of the data class that
     * has been populated from the array of strings.
     *
     * For more information on how exactly this functions, please take a look at the wiki.
     *
     * @param dataclass Data class to parse Strings into.
     * @param args Array of Strings to parse.
     *
     * @throws ParseException Thrown if the arguments couldn't be parsed for some reason.
     */
    @Throws(ParseException::class)
    suspend fun <T : Any> parse(dataclass: KClass<T>, args: Array<out String>, event: MessageCreateEvent): T {
        if (!dataclass.isData) {
            throw ParseException("Given class is not a data class.")
        }

        val dcArgs = doParse(dataclass, args, event, dataclass.primaryConstructor!!.parameters)

        @Suppress("TooGenericExceptionCaught")
        return try {
            dataclass.primaryConstructor!!.callBy(dcArgs)
        } catch (e: IllegalArgumentException) {
            if (e.toString().contains("No argument provided for a required parameter")) {
                throw ParseException("A required argument is missing.")
            }

            throw e
        } catch (e: Exception) {
            throw ParseException("Data class cannot be constructed ($e)")
        }
    }

    @Throws(ParseException::class)
    private suspend fun <T : Any> doParse(
        dataclass: KClass<T>,
        args: Array<out String>,
        event: MessageCreateEvent,
        elements: List<KParameter>,

        argIndex: Int = 0,
        elementIndex: Int = 0,
        dcArgs: MutableMap<KParameter, Any?> = mutableMapOf()
    ): Map<KParameter, Any?> {
        if (argIndex >= args.size || elementIndex >= elements.size) {
            return dcArgs
        }

        val argument = args[argIndex]
        val element = elements[elementIndex]

        @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
        try {
            if (argument.contains(':')) {
                // If the parameter have a `:`, we assign the value on the right to the parameter on the left.
                val (paramName, paramArg) = argument.split(':')
                val paramProperty = dataclass.primaryConstructor!!.parameters.single { it.name == paramName }

                dcArgs[paramProperty] = stringToType(paramArg, paramProperty.type, event)

                return doParse(dataclass, args, event, elements, argIndex + 1, elementIndex, dcArgs)
            } else if (element.type.isSubtypeOf(listType)) {
                dcArgs[element] = stringsToTypes(
                    args.sliceArray(argIndex until args.size),
                    element.type.arguments[0].type!!,
                    event
                )

                return dcArgs
            } else {
                dcArgs[element] = stringToType(argument, element.type, event)

                // Element index should ordinarily match arg index, but when an optional parameter is skipped,
                // they aren't synced anymore
                return doParse(dataclass, args, event, elements, argIndex + 1, elementIndex + 1, dcArgs)
            }
        } catch (e: ParseException) {
            throw e
        } catch (e: Exception) { // Anything can happen here, really
            if (!element.isOptional) {  // Optional params have default values and thus can be skipped
                logger.warn(e) { "Failed to convert argument: $argument" }
                throw ParseException("`$argument` cannot be converted to ${element.type} ($e)")
            }

            // We want to try this again with the next element
            return doParse(dataclass, args, event, elements, argIndex, elementIndex + 1, dcArgs)
        }
    }

    private suspend fun stringToType(string: String, type: KType, event: MessageCreateEvent): Any? {
        @Suppress("TooGenericExceptionCaught")  // As usual, anything can happen here.
        return when {
            type.isSubtypeOf(String::class.createType()) ||
                type.isSubtypeOf(String::class.createType(nullable = true)) -> string

            type.isSubtypeOf(Regex::class.createType()) ||
                type.isSubtypeOf(Regex::class.createType(nullable = true)) -> string.toRegex()

            type.isSubtypeOf(Boolean::class.createType()) ||
                type.isSubtypeOf(Boolean::class.createType(nullable = true)) -> string.toBoolean()

            type.isSubtypeOf(Int::class.createType()) ||
                type.isSubtypeOf(Int::class.createType(nullable = true)) -> string.toInt()

            type.isSubtypeOf(Short::class.createType()) ||
                type.isSubtypeOf(Short::class.createType(nullable = true)) -> string.toShort()

            type.isSubtypeOf(Long::class.createType()) ||
                type.isSubtypeOf(Long::class.createType(nullable = true)) -> string.toLong()

            type.isSubtypeOf(Float::class.createType()) ||
                type.isSubtypeOf(Float::class.createType(nullable = true)) -> string.toFloat()

            type.isSubtypeOf(Double::class.createType()) ||
                type.isSubtypeOf(Double::class.createType(nullable = true)) -> string.toDouble()

            type.isSubtypeOf(BigDecimal::class.createType()) ||
                type.isSubtypeOf(BigDecimal::class.createType(nullable = true)) -> string.toBigDecimal()

            type.isSubtypeOf(BigInteger::class.createType()) ||
                type.isSubtypeOf(BigInteger::class.createType(nullable = true)) -> string.toBigInteger()

            type.isSubtypeOf(Channel::class.createType()) -> {
                val parsedString = parseMention(string)

                bot.kord.getChannel(Snowflake(parseMention(parsedString)))
                    ?: throw ParseException("No such channel: `$parsedString`")
            }

            type.isSubtypeOf(Channel::class.createType(nullable = true)) -> {
                val parsedString = parseMention(string)

                bot.kord.getChannel(Snowflake(parseMention(parsedString)))
            }

            type.isSubtypeOf(GuildEmoji::class.createType()) -> {
                val parsedString = parseMention(string)

                event.message.getGuild()?.getEmoji(Snowflake(parsedString))
                    ?: throw ParseException("No such emoji: `$parsedString`")
            }

            type.isSubtypeOf(GuildEmoji::class.createType(nullable = true)) -> {
                val parsedString = parseMention(string)

                event.message.getGuild()?.getEmoji(Snowflake(parsedString))
            }

            type.isSubtypeOf(Guild::class.createType()) -> {
                val parsedString = parseMention(string)

                bot.kord.getGuild(Snowflake(parsedString))
                    ?: throw ParseException("No such guild: `$parsedString`")
            }

            type.isSubtypeOf(Guild::class.createType(nullable = true)) -> {
                val parsedString = parseMention(string)

                bot.kord.getGuild(Snowflake(parsedString))
            }

            type.isSubtypeOf(Role::class.createType()) -> {
                val parsedString = parseMention(string)

                event.message.getGuild()?.getRole(Snowflake(parsedString))
                    ?: throw ParseException("No such role: `$parsedString`")
            }

            type.isSubtypeOf(Role::class.createType(nullable = true)) -> {
                val parsedString = parseMention(string)

                event.message.getGuild()?.getRole(Snowflake(parsedString))
            }

            type.isSubtypeOf(User::class.createType()) -> {
                val parsedString = parseMention(string)

                bot.kord.getUser(Snowflake(parsedString))
                    ?: throw ParseException("No such user: `$parsedString`")
            }

            type.isSubtypeOf(User::class.createType(nullable = true)) -> {
                val parsedString = parseMention(string)

                bot.kord.getUser(Snowflake(parsedString))
            }

            else -> throw NotImplementedError("String conversion not supported for type: $type")
        }
    }

    private suspend fun stringsToTypes(strings: Array<out String>, type: KType, event: MessageCreateEvent): List<Any?> {
        val values: MutableList<Any?> = mutableListOf()

        strings.forEach { values.add(stringToType(it, type, event)) }

        return values
    }

    private fun parseMention(string: String): String {
        if (string.toLongOrNull() != null) {
            return string
        }

        val result = mentionRegex.matchEntire(string.trim())

        return when {
            result == null -> throw ParseException("Could not convert `$string` to an ID.")
            result.groups.size < 2 -> throw ParseException("Could not convert `$string` to an ID.")
            result.groups[1] == null -> throw ParseException("Could not convert `$string` to an ID.")

            else -> result.groups[1]!!.value
        }
    }
}
