package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.impl.*
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

fun Arguments.string(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required))


fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringListConverter(required))


fun Arguments.number(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberConverter(required, radix))


fun Arguments.numberList(displayName: String, required: Boolean = true, radix: Int = 10) =
    arg(displayName, NumberListConverter(required, radix))

fun Arguments.decimal(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalConverter(required))


fun Arguments.decimalList(displayName: String, required: Boolean = true) =
    arg(displayName, DecimalListConverter(required))
