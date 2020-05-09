package com.kotlindiscord.kord.extensions.commands

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Role
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

class ArgumentParser(val bot: ExtensibleBot) {
    private val listType = List::class.createType(arguments = listOf(KTypeProjection.STAR))

    suspend fun <T : Any> parse(dataclass: KClass<T>, args: Array<out String>): T? {
        if (!dataclass.isData || dataclass.primaryConstructor == null) {
            TODO("Raise exception here")
        }

        val dcArgs: MutableMap<KParameter, Any?> = mutableMapOf()

        dataclass.primaryConstructor!!.parameters.forEachIndexed { i, element ->
            println("$i, ${element.name}")
            println("$i, ${element.type}")
            println()

            when {
                element.type.isSubtypeOf(listType) -> dcArgs[element] = stringsToTypes(
                    args.sliceArray(i until args.size),
                    element.type.arguments[0].type!!
                )

                else -> dcArgs[element] = stringToType(args[i], element.type)
            }
        }

        dcArgs.forEach { println(it) }

        return dataclass.primaryConstructor?.callBy(dcArgs)
    }

    suspend fun stringToType(string: String, type: KType): Any? {
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

    suspend fun stringsToTypes(strings: Array<out String>, type: KType): List<Any?> {
        val values: MutableList<Any?> = mutableListOf()

        strings.forEach { values.add(stringToType(it, type)) }

        return values
    }
}
