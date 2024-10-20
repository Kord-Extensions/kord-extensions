/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.parsers

import dev.kordex.core.KordExException
import dev.kordex.core.i18n.generated.CoreTranslations
import dev.kordex.core.i18n.types.Key

/**
 * A base exception class for parsers.
 */
public open class BaseParserException : KordExException()

/**
 * Generic duration parser exception.
 *
 * @param error Human-readable error text.
 */
public open class DurationParserException(public open var error: Key) : BaseParserException() {
	override val message: String? = toString()

	override fun toString(): String = error.toString()
}

/**
 * Thrown when invalid time unit given to duration parser.
 *
 * @param unit Invalid unit.
 */
public class InvalidTimeUnitException(public val unit: String) : DurationParserException(
	CoreTranslations.Converters.Duration.Error.invalidUnit.withOrdinalPlaceholders(unit)
)
