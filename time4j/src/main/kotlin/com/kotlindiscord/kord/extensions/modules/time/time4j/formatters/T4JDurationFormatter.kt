package com.kotlindiscord.kord.extensions.modules.time.time4j.formatters

import com.ibm.icu.number.NumberFormatter
import com.ibm.icu.util.MeasureUnit
import com.kotlindiscord.kord.extensions.i18n.JoinerSettings
import com.kotlindiscord.kord.extensions.i18n.TranslationsProvider
import net.time4j.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.*

/**
 * Class in charge of formatting Time4J duration objects into human-readable form, taking locales and translations
 * into account.
 */
public object T4JDurationFormatter : KoinComponent {
    private val translations: TranslationsProvider by inject()

    public fun format(duration: Duration<IsoUnit>, locale: Locale): String? {
        val parts = mutableListOf<String>()

        // This function is pretty cursed, but then again, Time4J is pretty cursed.
        val formatter = Duration.Formatter.ofPattern("#################Y::#M::#D::#h::#m::#s")

        val now = PlainTimestamp.nowInSystemTime()
        val offsetTime = duration.addTo(PlainTimestamp.nowInSystemTime())

        val newDuration = Duration.`in`(
            CalendarUnit.YEARS,
            CalendarUnit.MONTHS,
            CalendarUnit.DAYS,
            ClockUnit.HOURS,
            ClockUnit.MINUTES,
            ClockUnit.SECONDS
        ).between(now, offsetTime).toTemporalAmount()

        val times = formatter.format(newDuration).split("::").toMutableList()

        val years = times.removeFirst().toLong()
        val months = times.removeFirst().toLong()
        val days = times.removeFirst().toLong()
        val hours = times.removeFirst().toLong()
        val minutes = times.removeFirst().toLong()
        val seconds = times.removeFirst().toLong()

        val baseFormatter = NumberFormatter.withLocale(locale).unitWidth(NumberFormatter.UnitWidth.FORMAL)

        if (years > 0) {
            parts.add(baseFormatter.unit(MeasureUnit.YEAR).format(years).toString())
        }

        if (months > 0) {
            parts.add(baseFormatter.unit(MeasureUnit.MONTH).format(months).toString())
        }

        if (days > 0) {
            parts.add(baseFormatter.unit(MeasureUnit.DAY).format(days).toString())
        }

        if (hours > 0) {
            parts.add(baseFormatter.unit(MeasureUnit.HOUR).format(hours).toString())
        }

        if (minutes > 0) {
            parts.add(baseFormatter.unit(MeasureUnit.MINUTE).format(minutes).toString())
        }

        if (seconds > 0) {
            parts.add(baseFormatter.unit(MeasureUnit.SECOND).format(seconds).toString())
        }

        if (parts.isEmpty()) return null

        val mainJoiner = JoinerSettings.fromName(
            translations.translate("settings.time.formatting.joiner", locale)
        ) ?: JoinerSettings.SPACE

        val lastJoiner = JoinerSettings.fromName(
            translations.translate("settings.time.formatting.joiner.last", locale)
        ) ?: JoinerSettings.SPACE

        var mainJoinerString = mainJoiner.toString(translations, locale) ?: ""

        if (mainJoiner.spaceBefore) {
            mainJoinerString = " $mainJoinerString"
        }

        if (mainJoiner.spaceAfter) {
            mainJoinerString += " "
        }

        var lastJoinerString = lastJoiner.toString(translations, locale) ?: ""

        if (lastJoiner.spaceBefore) {
            lastJoinerString = " $lastJoinerString"
        }

        if (lastJoiner.spaceAfter) {
            lastJoinerString += " "
        }

        return when (parts.size) {
            1 -> parts.first()
            2 -> parts.joinToString(lastJoinerString)

            else -> {
                val firstParts = parts.take(parts.size - 1)
                val lastPart = parts.last()

                firstParts.joinToString(mainJoinerString) + lastJoinerString + lastPart
            }
        }
    }
}
