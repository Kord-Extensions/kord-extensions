package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import kotlin.reflect.KClass

class EnumListConverter<E : Enum<E>>(
    required: Boolean = true,
    typeName: String,
    private val getter: (String) -> E?,
    enum: KClass<E>
) : MultiConverter<E>(required) {
    override val signatureTypeString: String = typeName

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val enums = mutableListOf<E>()

        for (arg in args) {
            try {
                enums.add(getter.invoke(arg) ?: break)
            } catch (e: IllegalArgumentException) {
                break
            }
        }

        this.parsed = enums.toList()

        return args.size
    }
}
