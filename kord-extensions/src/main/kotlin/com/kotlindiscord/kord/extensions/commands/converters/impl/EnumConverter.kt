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
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
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
@OptIn(KordPreview::class)
public class EnumConverter<E : Enum<E>>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    override var validator: (suspend Argument<*>.(E) -> Unit)? = null
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
 * Create an enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    description: String,
    typeName: String,
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): SingleConverter<T> = arg(displayName, description, EnumConverter(typeName, getter, validator))

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enum(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): SingleConverter<T> =
    enum(displayName, description, typeName, ::getEnum, validator)

/**
 * Create a defaulting enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    description: String,
    typeName: String,
    defaultValue: T,
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): DefaultingConverter<T> = arg(
    displayName,
    description,
    EnumConverter(typeName, getter)
        .toDefaulting(defaultValue, nestedValidator = validator)
)

/**
 * Create an enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.defaultingEnum(
    displayName: String,
    description: String,
    typeName: String,
    defaultValue: T,
    noinline validator: (suspend Argument<*>.(T) -> Unit)? = null,
): DefaultingConverter<T> =
    defaultingEnum(displayName, description, typeName, defaultValue, ::getEnum, validator)

/**
 * Create an optional enum converter, for single arguments - using a custom getter.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    description: String,
    typeName: String,
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(T?) -> Unit)? = null,
): OptionalConverter<T?> = arg(
    displayName,
    description,
    EnumConverter(typeName, getter)
        .toOptional(nestedValidator = validator)
)

/**
 * Create an optional enum converter, for single arguments - using the default getter, [getEnum].
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.optionalEnum(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: (suspend Argument<*>.(T?) -> Unit)? = null,
): OptionalConverter<T?> =
    optionalEnum<T>(displayName, description, typeName, ::getEnum, validator)

/**
 * Create an enum converter, for lists of arguments - using a custom getter.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    description: String,
    typeName: String,
    required: Boolean = true,
    noinline getter: suspend (String) -> T?,
    noinline validator: (suspend Argument<*>.(List<T>) -> Unit)? = null,
): MultiConverter<T> = arg(
    displayName,
    description,
    EnumConverter(typeName, getter)
        .toMulti(required, nestedValidator = validator)
)

/**
 * Create an enum converter, for lists of arguments - using the default getter, [getEnum].
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EnumConverter
 */
public inline fun <reified T : Enum<T>> Arguments.enumList(
    displayName: String,
    description: String,
    typeName: String,
    required: Boolean = true,
    noinline validator: (suspend Argument<*>.(List<T>) -> Unit)? = null,
): MultiConverter<T> =
    enumList<T>(displayName, description, typeName, required, ::getEnum, validator)

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
