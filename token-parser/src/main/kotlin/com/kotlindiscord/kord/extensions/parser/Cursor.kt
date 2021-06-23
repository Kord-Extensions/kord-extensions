package com.kotlindiscord.kord.extensions.parser

/**
 * Class representing an iteration position over a given string, with convenience functions. This is
 * intended for use with the token parsing system, but you can use it for other things too.
 *
 * @param input Input string to iterate over. Never modified.
 */
public class Cursor(public val input: String) {
    /** Current iteration index, starting at `-1`. **/
    public var index: Int = -1

    /** Returns `true` if there are more characters left to iterate over. **/
    public val hasNext: Boolean
        get() =
            index < input.length - 1

    /** Returns `true` if there are characters to iterate backwards to. **/
    public val hasPrevious: Boolean
        get() =
            index > 0

    /** Iterate over the rest of the string, returning the result. **/
    public fun consumeRemaining(): String {
        var result = ""

        while (hasNext) {
            result += next()
        }

        return result
    }

    /**
     *  Iterate over the rest of the string as long as the predicate returns `true`, returning the
     *  result.
     */
    public fun consumeWhile(predicate: (Char) -> Boolean): String? {
        var result: String? = null

        while (hasNext && predicate(peekNext()!!)) {
            result = (result ?: "") + next()
        }

        return result
    }

    /** Skip any immediate whitespace, updating the [index]. **/
    public fun skipWhitespace(): Boolean {
        if (peekNext()?.isWhitespace() != true) {
            return false
        }

        while (peekNext()?.isWhitespace() == true) {
            next()
        }

        return true
    }

    /** Increment the [index] and return the character found there, throwing if we're at the end of the string. **/
    public fun next(): Char {
        if (hasNext) {
            index += 1
            return input[index]
        }

        error("Cursor has no further elements.")
    }

    /** Increment the [index] and return the character found there, or `null` if we're at the end of the string. **/
    public fun nextOrNull(): Char? {
        if (hasNext) {
            index += 1
            return input[index]
        }

        return null
    }

    /** Decrement the [index] and return the character found there, throwing if we're at the start of the string. **/
    public fun previous(): Char {
        if (hasPrevious) {
            index -= 1
            return input[index]
        }

        error("Cursor has no previous elements.")
    }

    /** Decrement the [index] and return the character found there, or `null` if we're at the start of the string. **/
    public fun previousOrNull(): Char? {
        if (hasPrevious) {
            index -= 1
            return input[index]
        }

        return null
    }

    /** Return the character at the current index. **/
    public fun peek(): Char =
        input[index]

    /** Return the character at the next index, or `null` if we're at the end of the string. **/
    public fun peekNext(): Char? {
        if (hasNext) {
            return input[index + 1]
        }

        return null
    }

    /** Return the character at the previous index, or `null` if we're at the start of the string. **/
    public fun peekPrevious(): Char? {
        if (hasPrevious) {
            return input[index - 1]
        }

        return null
    }
}
