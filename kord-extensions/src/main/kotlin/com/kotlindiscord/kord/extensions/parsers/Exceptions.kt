package com.kotlindiscord.kord.extensions.parsers

import com.kotlindiscord.kord.extensions.ExtensionsException

/**
 * A base exception class for parsers.
 */
public open class BaseParserException : ExtensionsException()

/**
 * Throws when invalid time unit given to duration parser.
 *
 * @param unit Invalid unit.
 */
public class InvalidTimeUnitException(public var unit: String) : BaseParserException() {
    override fun toString(): String = "Invalid time unit provided: $unit"
}
