package com.kotlindiscord.kordex.ext.common.emoji

/**
 * Abstract class representing a named emoji. Implement this in your emoji enums, so the emoji extension can find them.
 *
 * @param name Name of the custom emoji, which is used to find the emoji on the configured guilds.
 * @param default String to use when the emoji can't be found instead, defaulting to `:name:`
 */
@Suppress("UnnecessaryAbstractClass")  // Interfaces can't have constructors
abstract class NamedEmoji(
    val name: String,
    val default: String = ":$name:"
)
