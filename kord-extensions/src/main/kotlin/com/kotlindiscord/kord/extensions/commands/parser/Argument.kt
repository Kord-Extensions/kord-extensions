package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.commands.converters.Converter

data class Argument<T : Any>(val displayName: String, val converter: Converter<T>)
