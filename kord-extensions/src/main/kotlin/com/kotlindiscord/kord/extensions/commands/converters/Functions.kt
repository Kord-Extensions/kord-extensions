package com.kotlindiscord.kord.extensions.commands.converters

import com.kotlindiscord.kord.extensions.commands.converters.impl.StringConverter
import com.kotlindiscord.kord.extensions.commands.converters.impl.StringListConverter
import com.kotlindiscord.kord.extensions.commands.parser.Arguments

fun Arguments.string(displayName: String, required: Boolean = true) =
    arg(displayName, StringConverter(required))


fun Arguments.stringList(displayName: String, required: Boolean = true) =
    arg(displayName, StringListConverter(required))
