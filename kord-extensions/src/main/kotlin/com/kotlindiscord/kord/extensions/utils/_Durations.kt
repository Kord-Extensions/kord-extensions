/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.utils

import com.ibm.icu.text.MeasureFormat
import com.ibm.icu.util.Measure
import com.ibm.icu.util.MeasureUnit
import kotlinx.datetime.DateTimePeriod
import java.util.*

/**
 * Convenience function for formatting a [DateTimePeriod] in the given [locale].
 */
@Suppress("MagicNumber")
public fun DateTimePeriod.format(locale: Locale): String {
    val fmt = MeasureFormat.getInstance(locale, MeasureFormat.FormatWidth.WIDE)
    val measures: MutableList<Measure> = mutableListOf()

    val weeks = days.floorDiv(7)
    val remainingDays = days % 7

    if (years != 0) measures.add(Measure(years, MeasureUnit.YEAR))
    if (months != 0) measures.add(Measure(months, MeasureUnit.MONTH))
    if (weeks != 0) measures.add(Measure(weeks, MeasureUnit.WEEK))
    if (remainingDays != 0) measures.add(Measure(remainingDays, MeasureUnit.DAY))
    if (hours != 0) measures.add(Measure(hours, MeasureUnit.HOUR))
    if (minutes != 0) measures.add(Measure(minutes, MeasureUnit.MINUTE))
    if (seconds != 0) measures.add(Measure(seconds, MeasureUnit.SECOND))

    @Suppress("SpreadOperator")  // There's no other way, really
    return fmt.formatMeasures(*measures.toTypedArray())
}
