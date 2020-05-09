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
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

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

    private val mentionRegex = Regex("^<[@#][!&]?(\\d+)>$")

    /**
     * Given a data class and an array of strings, return an instance of the data class that
     * has been populated from the array of strings.
     *
     * This function needs a lot of work:
     *     TODO: Raise exception if not data class
     *     TODO: Think about exception handling for conversions
     *     TODO: Think about null handling and default arguments
     *
     * @param dataclass Data class to parse Strings into.
     * @param args Array of Strings to parse.
     *
     * @throws ParseException Thrown if the arguments couldn't be parsed.
     */
    @Throws(ParseException::class)
    suspend fun <T : Any> parse(dataclass: KClass<T>, args: Array<out String>, event: MessageCreateEvent): T {
        if (!dataclass.isData) {
            throw ParseException("Failed to parse arguments: Given class is not a data class.")
        }

        if (dataclass.primaryConstructor == null) {
            throw ParseException("Failed to parse arguments: Given class has no primary constructor.")
        }

        val dcArgs: MutableMap<KParameter, Any?> = mutableMapOf()

        dataclass.primaryConstructor!!.parameters.forEachIndexed { i, element ->
            @Suppress("TooGenericExceptionCaught", "RethrowCaughtException")
            try {
                if (element.type.isSubtypeOf(listType)) {
                    dcArgs[element] = stringsToTypes(
                        args.sliceArray(i until args.size),
                        element.type.arguments[0].type!!,
                        event
                    )
                } else {
                    dcArgs[element] = stringToType(args[i], element.type, event)
                }
            } catch (e: ParseException) {
                throw e
            } catch (e: Exception) { // Anything can happen here, really
                e.printStackTrace()
                throw ParseException("`${args[i]}` cannot be converted to ${element.type} ($e)")
            }
        }

        @Suppress("TooGenericExceptionCaught")
        return try {
            dataclass.primaryConstructor!!.callBy(dcArgs)
        } catch (e: Exception) {
            throw ParseException("Failed to parse arguments: Data class cannot be constructed ($e)")
        }
    }

    private suspend fun stringToType(string: String, type: KType, event: MessageCreateEvent): Any? {
        // TODO: Nullable type checking/appropriate exceptions
        return when {
            type.isSubtypeOf(String::class.createType()) -> string

            type.isSubtypeOf(Int::class.createType()) -> string.toInt()
            type.isSubtypeOf(Long::class.createType()) -> string.toLong()
            type.isSubtypeOf(Float::class.createType()) -> string.toFloat()

            type.isSubtypeOf(Channel::class.createType()) -> {
                val parsedString = parseMention(string)

                bot.kord.getChannel(Snowflake(parseMention(parsedString)))
                    ?: throw ParseException("No such channel: `$parsedString`")
            }

            type.isSubtypeOf(GuildEmoji::class.createType()) -> {
                val parsedString = parseMention(string)

                event.message.getGuild()?.getEmoji(Snowflake(parsedString))
                    ?: throw ParseException("No such emoji: `$parsedString`")
            }

            type.isSubtypeOf(Guild::class.createType()) -> {
                val parsedString = parseMention(string)

                bot.kord.getGuild(Snowflake(parsedString))
                    ?: throw ParseException("No such guild: `$parsedString`")
            }

            type.isSubtypeOf(Role::class.createType()) -> {
                val parsedString = parseMention(string)

                event.message.getGuild()?.getRole(Snowflake(parsedString))
                    ?: throw ParseException("No such role: `$parsedString`")
            }

            type.isSubtypeOf(User::class.createType()) -> {
                val parsedString = parseMention(string)

                bot.kord.getUser(Snowflake(parsedString))
                    ?: throw ParseException("No such user: `$parsedString`")
            }
//            type.isSubtypeOf(Snowflake::class.createType()) -> Snowflake(string)

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
