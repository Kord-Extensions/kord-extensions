/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.time

/**
 * Sealed class representing the different types of Discord-formatted timestamps.
 *
 * @property string Suffix to add to the string to get Discord's respective format, with colon prefix
 */
public sealed class TimestampType(public val string: String?) {
	/** Let Discord figure out what format to use. **/
	public object Default : TimestampType(null)

	/** A short date and time. **/
	public object ShortDateTime : TimestampType(":f")

	/** A long date and time. **/
	public object LongDateTime : TimestampType(":F")

	/** A short date. **/
	public object ShortDate : TimestampType(":d")

	/** A long date. **/
	public object LongDate : TimestampType(":D")

	/** A short time. **/
	public object ShortTime : TimestampType(":t")

	/** A long time. **/
	public object LongTime : TimestampType(":T")

	/** A time, displayed relative to the current time. **/
	public object RelativeTime : TimestampType(":R")

	/** Format the given [Long] value according to the current timestamp type. **/
	public fun format(value: Long): String = "<t:$value${string ?: ""}>"

	public companion object {
		/**
		 * Parse Discord's format specifiers to a specific format.
		 */
		public fun fromFormatSpecifier(string: String?): TimestampType? {
			return when (string) {
				"f" -> ShortDateTime
				"F" -> LongDateTime
				"d" -> ShortDate
				"D" -> LongDate
				"t" -> ShortTime
				"T" -> LongTime
				"R" -> RelativeTime
				null -> Default
				else -> null
			}
		}
	}
}
