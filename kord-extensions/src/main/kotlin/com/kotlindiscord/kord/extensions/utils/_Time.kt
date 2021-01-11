@file:JvmMultifileClass
@file:JvmName("TimeKt")

package com.kotlindiscord.kord.extensions.utils

import net.time4j.Duration
import net.time4j.IsoUnit
import java.lang.StringBuilder
import java.time.temporal.ChronoUnit

/**
 * Convert a Time4J Duration object to seconds.
 *
 * @return The duration object folded into a single Long, representing total seconds.
 */
public fun Duration<IsoUnit>.toSeconds(): Long {
    val amount = this.toTemporalAmount()
    var seconds = 0L

    seconds += amount.get(ChronoUnit.MILLENNIA) * ChronoUnit.MILLENNIA.duration.seconds
    seconds += amount.get(ChronoUnit.CENTURIES) * ChronoUnit.CENTURIES.duration.seconds
    seconds += amount.get(ChronoUnit.DECADES) * ChronoUnit.DECADES.duration.seconds
    seconds += amount.get(ChronoUnit.YEARS) * ChronoUnit.YEARS.duration.seconds
    seconds += amount.get(ChronoUnit.MONTHS) * ChronoUnit.MONTHS.duration.seconds
    seconds += amount.get(ChronoUnit.WEEKS) * ChronoUnit.WEEKS.duration.seconds
    seconds += amount.get(ChronoUnit.DAYS) * ChronoUnit.DAYS.duration.seconds
    seconds += amount.get(ChronoUnit.HOURS) * ChronoUnit.HOURS.duration.seconds
    seconds += amount.get(ChronoUnit.MINUTES) * ChronoUnit.MINUTES.duration.seconds
    seconds += amount.get(ChronoUnit.SECONDS)

    return seconds
}

/**
 * Given a Duration, this function will return a String (or null if it represents less than 1 second).
 *
 * The string is intended to be readable for humans - "a days, b hours, c minutes, d seconds".
 */
@Suppress("MagicNumber")  // These are all time units!
public fun java.time.Duration.toHuman(): String? {
    if(isZero) return null
    
    val seconds = this.seconds % 60
    val minutesTotal = this.seconds / 60

    val minutes = minutesTotal % 60
    val hoursTotal = minutesTotal / 60

    val hours = hoursTotal % 24
    val days = hoursTotal / 24

    val builder = StringBuilder()
    fun addToString(value: Long, title: String){
        if(value > 0) {
            if (builder.isNotEmpty()) {
                builder.append(", ")
            }
            builder.append("$value $title${if (value > 1) 's' else ""}")
        }
    }
    
    addToString(days, "day")
    addToString(hours, "hour")
    addToString(minutes, "minute")
    addToString(seconds, "second")

    return builder.toString()
}
