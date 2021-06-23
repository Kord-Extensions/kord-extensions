package com.kotlindiscord.kord.extensions.parser.tokens

/**
 * Simple base class for a parser token. Exists in order to make changes later easier.
 */
public interface Token<T : Any?> {
    /** Stored token data. **/
    public val data: T
}
