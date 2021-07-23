package com.kotlindiscord.kord.extensions.parser.tokens

/**
 * Data class representing a single named argument token.
 *
 * @param name Token name
 * @param data Argument data
 */
public data class NamedArgumentToken(
    public val name: String,
    override val data: String
) : Token<String>
