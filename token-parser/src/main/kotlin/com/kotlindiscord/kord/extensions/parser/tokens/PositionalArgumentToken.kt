package com.kotlindiscord.kord.extensions.parser.tokens

/**
 * Data class representing a single positional argument token.
 *
 * @param data Argument data
 */
public data class PositionalArgumentToken(
    override val data: String
) : Token<String>
