@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.slash.converters

import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.converters.impl.EnumChoiceConverter
import com.kotlindiscord.kord.extensions.commands.slash.converters.impl.NumberChoiceConverter
import com.kotlindiscord.kord.extensions.commands.slash.converters.impl.StringChoiceConverter
import dev.kord.common.annotation.KordPreview

// region: Required (single) converters

/**
 * Create an enum choice argument converter, for a defined set of single arguments.
 *
 * @see EnumChoiceConverter
 */
public inline fun <reified T> Arguments.enumChoice(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: (suspend (T) -> Unit)? = null,
): SingleConverter<T> where T : Enum<T>, T : ChoiceEnum = arg(
    displayName,
    description,
    EnumChoiceConverter(typeName, ::getEnum, enumValues(), validator)
)

/**
 * Create a number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.numberChoice(
    displayName: String,
    description: String,
    choices: Map<String, Int>,
    radix: Int = 10,
    validator: (suspend (Int) -> Unit)? = null
): SingleConverter<Int> = arg(displayName, description, NumberChoiceConverter(radix, choices, validator))

/**
 * Create a string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.stringChoice(
    displayName: String,
    description: String,
    choices: Map<String, String>,
    validator: (suspend (String) -> Unit)? = null
): SingleConverter<String> = arg(displayName, description, StringChoiceConverter(choices, validator))

// endregion

// region: Optional converters

/**
 * Create an optional enum choice argument converter, for a defined set of single arguments.
 *
 * @see EnumChoiceConverter
 */
public inline fun <reified T> Arguments.optionalEnumChoice(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: (suspend (T?) -> Unit)? = null,
): OptionalConverter<T?> where T : Enum<T>, T : ChoiceEnum = arg(
    displayName,
    description,
    EnumChoiceConverter<T>(typeName, ::getEnum, enumValues())
            .toOptional(nestedValidator = validator)
)

/**
 * Create an optional number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.optionalNumberChoice(
    displayName: String,
    description: String,
    choices: Map<String, Int>,
    radix: Int = 10,
    validator: (suspend (Int?) -> Unit)? = null
): OptionalConverter<Int?> = arg(
    displayName,
    description,
    NumberChoiceConverter(radix, choices)
            .toOptional(nestedValidator = validator)
)

/**
 * Create an optional string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.optionalStringChoice(
    displayName: String,
    description: String,
    choices: Map<String, String>,
    validator: (suspend (String?) -> Unit)? = null
): OptionalConverter<String?> = arg(
    displayName,
    description,
    StringChoiceConverter(choices)
            .toOptional(nestedValidator = validator)
)

// endregion

// region: Defaulting converters

/**
 * Create a defaulting enum choice argument converter, for a defined set of single arguments.
 *
 * @see EnumChoiceConverter
 */
public inline fun <reified T> Arguments.defaultingEnumChoice(
    displayName: String,
    description: String,
    typeName: String,
    defaultValue: T,
    noinline validator: (suspend (T) -> Unit)? = null,
): DefaultingConverter<T> where T : Enum<T>, T : ChoiceEnum = arg(
    displayName,
    description,
    EnumChoiceConverter<T>(typeName, ::getEnum, enumValues())
            .toDefaulting(defaultValue, nestedValidator = validator)
)

/**
 * Create a defaulting number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.defaultingNumberChoice(
    displayName: String,
    description: String,
    defaultValue: Int,
    choices: Map<String, Int>,
    radix: Int = 10,
    validator: (suspend (Int) -> Unit)? = null
): DefaultingConverter<Int> = arg(
    displayName,
    description,
    NumberChoiceConverter(radix, choices)
            .toDefaulting(defaultValue, nestedValidator = validator)
)

/**
 * Create a defaulting string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.defaultingStringChoice(
    displayName: String,
    description: String,
    defaultValue: String,
    choices: Map<String, String>,
    validator: (suspend (String) -> Unit)? = null
): DefaultingConverter<String> = arg(
    displayName,
    description,
    StringChoiceConverter(choices)
            .toDefaulting(defaultValue, nestedValidator = validator)
)

// endregion

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
