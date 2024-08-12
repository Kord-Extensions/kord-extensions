/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers

import dev.kordex.core.KordExException

/**
 * A base exception class for parsers.
 */
public open class BaseParserException : KordExException()

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
