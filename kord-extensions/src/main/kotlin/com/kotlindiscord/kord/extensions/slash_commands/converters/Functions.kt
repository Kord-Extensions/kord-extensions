@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.slash_commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.DefaultingConverter
import com.kotlindiscord.kord.extensions.commands.converters.OptionalConverter
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.slash_commands.converters.impl.NumberChoiceConverter
import com.kotlindiscord.kord.extensions.slash_commands.converters.impl.StringChoiceConverter
import dev.kord.common.annotation.KordPreview

// region: Required (single) converters

/**
 * Create a number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.numberChoice(
    displayName: String,
    description: String,
    choices: Map<String, Int>,
    radix: Int = 10
): SingleConverter<Int> = arg(displayName, description, NumberChoiceConverter(radix, choices))

/**
 * Create a string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.stringChoice(
    displayName: String,
    description: String,
    choices: Map<String, String>
): SingleConverter<String> = arg(displayName, description, StringChoiceConverter(choices))

// endregion

// region: Optional converters

/**
 * Create an optional number choice argument converter, for a defined set of single arguments.
 *
 * @see NumberChoiceConverter
 */
public fun Arguments.optionalNumberChoice(
    displayName: String,
    description: String,
    choices: Map<String, Int>,
    radix: Int = 10
): OptionalConverter<Int?> = arg(displayName, description, NumberChoiceConverter(radix, choices).toOptional())

/**
 * Create an optional string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.optionalStringChoice(
    displayName: String,
    description: String,
    choices: Map<String, String>
): OptionalConverter<String?> = arg(displayName, description, StringChoiceConverter(choices).toOptional())

// endregion

// region: Defaulting converters

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
    radix: Int = 10
): DefaultingConverter<Int> =
    arg(displayName, description, NumberChoiceConverter(radix, choices).toDefaulting(defaultValue))

/**
 * Create a defaulting string choice argument converter, for a defined set of single arguments.
 *
 * @see StringChoiceConverter
 */
public fun Arguments.defaultingStringChoice(
    displayName: String,
    description: String,
    defaultValue: String,
    choices: Map<String, String>
): DefaultingConverter<String> =
    arg(displayName, description, StringChoiceConverter(choices).toDefaulting(defaultValue))

// endregion
