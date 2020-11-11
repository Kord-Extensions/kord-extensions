package com.kotlindiscord.kord.extensions.commands.parser

data class Argument<T: Any>(val displayName: String, val converter: Converter<T>)
