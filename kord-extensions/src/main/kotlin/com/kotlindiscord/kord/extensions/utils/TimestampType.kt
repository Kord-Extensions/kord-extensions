package com.kotlindiscord.kord.extensions.utils

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
}
