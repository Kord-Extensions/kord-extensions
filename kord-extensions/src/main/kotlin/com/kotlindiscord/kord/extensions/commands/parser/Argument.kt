package com.kotlindiscord.kord.extensions.commands.parser

import com.kotlindiscord.kord.extensions.commands.converters.Converter

/**
 * Data class representing a single argument.
 *
 * @param displayName Name shown on Discord in help messages, and used for keyword arguments.
 * @param converter Argument converter to use for this argument.
 */
data class Argument<T : Any>(val displayName: String, val converter: Converter<T>)
