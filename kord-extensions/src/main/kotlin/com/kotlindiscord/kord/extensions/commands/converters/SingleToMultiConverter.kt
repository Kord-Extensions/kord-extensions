package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

class SingleToMultiConverter<T: Any>(
    required: Boolean = true,
    val singleConverter: SingleConverter<T>,

    newSignatureTypeString: String? = null,
    newShowTypeInSignature: Boolean? = null,
    newErrorTypeString: String? = null
): MultiConverter<T>(required) {
    override val signatureTypeString: String = newSignatureTypeString ?: singleConverter.signatureTypeString
    override val showTypeInSignature: Boolean = newShowTypeInSignature ?: singleConverter.showTypeInSignature
    override val errorTypeString: String? = newErrorTypeString ?: singleConverter.errorTypeString

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val values = mutableListOf<T>()
        val dummyArgs = Arguments()

        for (arg in args) {
            try {
                val result = singleConverter.parse(arg, context, bot)

                if (!result) {
                    break
                }

                val value = singleConverter.getValue(dummyArgs, singleConverter::parsed) ?: break

                values.add(value)
            } catch (e: ParseException) {
                break
            }
        }

        parsed = values

        return parsed.size
    }

    override suspend fun handleError(t: Throwable, values: List<String>, context: CommandContext, bot: ExtensibleBot): String {
        return singleConverter.handleError(t, null, context, bot)
    }
}
