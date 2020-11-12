package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import java.lang.IllegalArgumentException
import kotlin.reflect.KClass
import kotlin.reflect.jvm.jvmName

class EnumConverter<E : Enum<E>>(
    required: Boolean = true,
    typeName: String,
    private val getter: (String) -> E?,
    enum: KClass<E>
) : SingleConverter<E>(required) {
    override val signatureTypeString: String = typeName

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            parsed = getter.invoke(arg) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }
}
