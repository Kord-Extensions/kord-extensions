@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Choice converter for enum arguments. Supports mapping up to 25 choices to an enum type.
 *
 * All enums used for this must implement the [ChoiceEnum] interface.
 */
@OptIn(KordPreview::class)
public class EnumChoiceConverter<E>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    choices: Array<E>,
    override var validator: Validator<E> = null,
    override val bundle: String? = null,
) : ChoiceConverter<E>(choices.associateBy { it.readableName }) where E : Enum<E>, E : ChoiceEnum {
    override val signatureTypeString: String = typeName

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        try {
            parsed = getter.invoke(arg) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@EnumChoiceConverter.choices.forEach { choice(it.key, it.value.name) }
        }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val stringOption = option as? OptionValue.StringOptionValue ?: return false

        try {
            parsed = getter.invoke(stringOption.value) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }
}

/**
 * Create an enum choice argument converter, for a defined set of single arguments.
 *
 * @see EnumChoiceConverter
 */
public inline fun <reified T> Arguments.enumChoice(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: Validator<T> = null,
    bundle: String? = null,
): SingleConverter<T> where T : Enum<T>, T : ChoiceEnum = arg(
    displayName,
    description,
    EnumChoiceConverter(typeName, ::getEnum, enumValues(), validator, bundle = bundle)
)

/**
 * Create an optional enum choice argument converter, for a defined set of single arguments.
 *
 * @see EnumChoiceConverter
 */
public inline fun <reified T> Arguments.optionalEnumChoice(
    displayName: String,
    description: String,
    typeName: String,
    noinline validator: Validator<T?> = null,
    bundle: String? = null,
): OptionalConverter<T?> where T : Enum<T>, T : ChoiceEnum = arg(
    displayName,
    description,
    EnumChoiceConverter<T>(typeName, ::getEnum, enumValues(), bundle = bundle)
        .toOptional(nestedValidator = validator)
)

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
    noinline validator: Validator<T> = null,
    bundle: String? = null,
): DefaultingConverter<T> where T : Enum<T>, T : ChoiceEnum = arg(
    displayName,
    description,
    EnumChoiceConverter<T>(typeName, ::getEnum, enumValues(), bundle = bundle)
        .toDefaulting(defaultValue, nestedValidator = validator)
)

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
