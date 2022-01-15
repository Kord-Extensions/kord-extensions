/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.application.slash.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToDefaulting
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToMulti
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
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
@Converter(
    "enum",

    types = [ConverterType.SINGLE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.CHOICE],
    imports = [
        "com.kotlindiscord.kord.extensions.commands.converters.impl.getEnum",
        "com.kotlindiscord.kord.extensions.commands.application.slash.converters.ChoiceEnum"
    ],

    builderGeneric = "E",
    builderConstructorArguments = [
        "public var getter: suspend (String) -> E?",
        "!! argMap: Map<String, E>",
    ],

    builderFields = [
        "public lateinit var typeName: String",
        "public var bundle: String? = null"
    ],

    builderInitStatements = [
        "choices(argMap)"
    ],

    builderSuffixedWhere = "E : Enum<E>, E : ChoiceEnum",

    functionGeneric = "E",
    functionBuilderArguments = [
        "getter = { getEnum(it) }",
        "argMap = enumValues<E>().associateBy { it.readableName }",
    ],

    functionSuffixedWhere = "E : Enum<E>, E : ChoiceEnum"
)
@OptIn(KordPreview::class)
public class EnumChoiceConverter<E>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    choices: Map<String, E>,
    override var validator: Validator<E> = null,
    override val bundle: String? = null,
) : ChoiceConverter<E>(choices) where E : Enum<E>, E : ChoiceEnum {
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
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified T : Enum<T>> getEnum(arg: String): T? =
    enumValues<T>().firstOrNull {
        it.name.equals(arg, true)
    }
