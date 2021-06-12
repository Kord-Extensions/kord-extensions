@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for arbitrary enum arguments.
 *
 * As this converter is generic, it takes a getter lambda. You can either provide one yourself, or use the default
 * one via the provided extension functions - the default getter simply checks for case-insensitive matches on enum
 * names.
 *
 * @see enum
 * @see enumList
 */
@Converter(
    name = "enum",
    types = [ConverterType.SINGLE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.LIST],

    generic = "E: Enum<E>",
    imports = ["com.kotlindiscord.kord.extensions.commands.converters.impl.getEnum"],
    arguments = [
        "typeName: String",
        "noinline getter: suspend (String) -> E? = { getEnum<E>(it) }"
    ]
)
@OptIn(KordPreview::class)
public class EnumConverter<E : Enum<E>>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    override var validator: Validator<E> = null
) : SingleConverter<E>() {
    override val signatureTypeString: String = typeName

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            parsed = getter.invoke(arg) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified E : Enum<E>> getEnum(arg: String): E? =
    enumValues<E>().firstOrNull {
        it.name.equals(arg, true)
    }
