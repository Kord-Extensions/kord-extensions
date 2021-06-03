package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.ExtensionsException

/**
 * A base exception class for parsers.
 */
public open class BaseParserException : ExtensionsException()

/**
 * Generic duration parser exception.
 *
 * @param error Human-readable error text.
 */
public open class DurationParserException(public open var error: String) : BaseParserException() {
    override val message: String? = error
    override fun toString(): String = error
}

/**
 * Thrown when invalid time unit given to duration parser.
 *
 * @param unit Invalid unit.
 */
public class InvalidTimeUnitException(public val unit: String) : DurationParserException(unit)
