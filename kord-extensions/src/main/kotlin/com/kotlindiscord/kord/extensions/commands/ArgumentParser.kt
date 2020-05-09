package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.User
import com.gitlab.kordlib.core.entity.channel.Channel
import com.kotlindiscord.kord.extensions.ExtensibleBot
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
     */
    suspend fun <T : Any> parse(dataclass: KClass<T>, args: Array<out String>): T? {
        if (!dataclass.isData || dataclass.primaryConstructor == null) {
            TODO("Raise exception here")
        }

        val dcArgs: MutableMap<KParameter, Any?> = mutableMapOf()

        dataclass.primaryConstructor!!.parameters.forEachIndexed { i, element ->
            println("$i, ${element.name}")
            println("$i, ${element.type}")
            println()

            if (element.type.isSubtypeOf(listType)) {
                dcArgs[element] = stringsToTypes(
                    args.sliceArray(i until args.size),
                    element.type.arguments[0].type!!
                )
            } else {
                dcArgs[element] = stringToType(args[i], element.type)
            }
        }

        dcArgs.forEach { println(it) }

        return dataclass.primaryConstructor?.callBy(dcArgs)
    }

    private suspend fun stringToType(string: String, type: KType): Any? {
        return when {
            type.isSubtypeOf(Int::class.createType()) -> string.toInt()
            type.isSubtypeOf(Long::class.createType()) -> string.toLong()
            type.isSubtypeOf(Float::class.createType()) -> string.toFloat()

            type.isSubtypeOf(String::class.createType()) -> string

            type.isSubtypeOf(Channel::class.createType()) -> bot.kord.getChannel(Snowflake(string))
            type.isSubtypeOf(User::class.createType()) -> bot.kord.getUser(Snowflake(string))
//            type.isSubtypeOf(Snowflake::class.createType()) -> Snowflake(string)

            else -> throw NotImplementedError("String conversion not supported for type: $type")
        }
    }

    private suspend fun stringsToTypes(strings: Array<out String>, type: KType): List<Any?> {
        val values: MutableList<Any?> = mutableListOf()

        strings.forEach { values.add(stringToType(it, type)) }

        return values
    }
}
