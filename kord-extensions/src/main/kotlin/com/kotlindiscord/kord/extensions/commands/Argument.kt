package com.kotlindiscord.kord.extensions.commands

import com.kotlindiscord.kord.extensions.commands.converters.Converter

/**
 * Data class representing a single argument.
 *
 * @param displayName Name shown on Discord in help messages, and used for keyword arguments.
 * @param description Short description explaining what the argument does.
 * @param converter Argument converter to use for this argument.
 */
public data class Argument<T : Any?>(
    val displayName: String,
    val description: String,
    val converter: Converter<T, *, *, *>
) {
    init {
        converter.argumentObj = this
    }
}
