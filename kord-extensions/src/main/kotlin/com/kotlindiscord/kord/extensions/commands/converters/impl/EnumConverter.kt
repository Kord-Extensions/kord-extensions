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

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToDefaulting
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToMulti
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
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
    "enum",

    types = [ConverterType.SINGLE, ConverterType.DEFAULTING, ConverterType.OPTIONAL, ConverterType.LIST],
    imports = ["com.kotlindiscord.kord.extensions.commands.converters.impl.getEnum"],

    builderGeneric = "E: Enum<E>",
    builderConstructorArguments = [
        "public var getter: suspend (String) -> E?"
    ],

    builderFields = [
        "public lateinit var typeName: String",
        "public var bundle: String? = null"
    ],

    functionGeneric = "E: Enum<E>",
    functionBuilderArguments = [
        "getter = { getEnum(it) }",
    ]
)
@OptIn(KordPreview::class)
public class EnumConverter<E : Enum<E>>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    override val bundle: String? = null,
    override var validator: Validator<E> = null
) : SingleConverter<E>() {
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
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false

        try {
            parsed = getter.invoke(optionValue) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }
}

/**
 * The default enum value getter - matches enums based on a case-insensitive string comparison with the name.
 */
public inline fun <reified E : Enum<E>> getEnum(arg: String): E? =
    enumValues<E>().firstOrNull {
        it.name.equals(arg, true)
    }
