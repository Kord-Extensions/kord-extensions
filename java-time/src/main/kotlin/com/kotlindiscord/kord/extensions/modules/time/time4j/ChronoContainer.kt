package com.kotlindiscord.kord.extensions.modules.time.time4j

import com.kotlindiscord.kord.extensions.parsers.InvalidTimeUnitException
import java.time.*
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal

private val SUPPORTED_UNITS = arrayOf(
    ChronoUnit.SECONDS,
    ChronoUnit.MINUTES,
    ChronoUnit.HOURS,

    ChronoUnit.DAYS,
    ChronoUnit.MONTHS,
    ChronoUnit.YEARS
)

private val CONVERTED_UNITS = arrayOf(
    ChronoUnit.WEEKS,
    ChronoUnit.DECADES,
    ChronoUnit.CENTURIES,
    ChronoUnit.MILLENNIA
)

private const val DAYS_PER_WEEK = 7L
private const val YEARS_PER_DECADE = 10L
private const val YEARS_PER_CENTURY = 100L
private const val YEARS_PER_MILLENNIUM = 1000L

private const val HOURS_PER_DAY = 24L
private const val MINUTES_PER_HOUR = 60L
private const val SECONDS_PER_MINUTE = 60L

/**
 * Class storing time units against values, to be applied to a time later on.
 *
 * This is intended as a way to apply a mixed-unit duration to a time when you don't actually know what time you'll
 * be applying it to. This is more accurate than straight up returning Java [java.time.Duration] objects, which can
 * only store a count of seconds.
 */
public class ChronoContainer {
    private val values: MutableMap<ChronoUnit, Long> = mutableMapOf()

    /** Check whether a given [ChronoUnit] is fully supported. **/
    public fun isSupported(unit: ChronoUnit): Boolean = unit in SUPPORTED_UNITS

    /** Check whether a given [ChronoUnit] will be converted in [plus], [minus] and [set]. **/
    public fun isConverted(unit: ChronoUnit): Boolean = unit in CONVERTED_UNITS

    /**
     * Given a value and [ChronoUnit], add it to this container's collection of values.
     *
     * This does not transform the value via abs(), so negative values are supported.
     */
    public fun plus(value: Long, unit: ChronoUnit) {
        when (unit) {
            in SUPPORTED_UNITS -> values[unit] = (values[unit] ?: 0) + value

            ChronoUnit.WEEKS -> plus(value * DAYS_PER_WEEK, ChronoUnit.DAYS)
            ChronoUnit.DECADES -> plus(value * YEARS_PER_DECADE, ChronoUnit.YEARS)
            ChronoUnit.CENTURIES -> plus(value * YEARS_PER_CENTURY, ChronoUnit.YEARS)
            ChronoUnit.MILLENNIA -> plus(value * YEARS_PER_MILLENNIUM, ChronoUnit.YEARS)

            else -> throw InvalidTimeUnitException(unit.name)
        }
    }

    /**
     * Given a value and [ChronoUnit], subtract it from this container's collection of values.
     *
     * This does not transform the value via abs(), so negative values are supported.
     */
    public fun minus(value: Long, unit: ChronoUnit) {
        when (unit) {
            in SUPPORTED_UNITS -> values[unit] = (values[unit] ?: 0) - value

            ChronoUnit.WEEKS -> minus(value * DAYS_PER_WEEK, ChronoUnit.DAYS)
            ChronoUnit.DECADES -> minus(value * YEARS_PER_DECADE, ChronoUnit.YEARS)
            ChronoUnit.CENTURIES -> minus(value * YEARS_PER_CENTURY, ChronoUnit.YEARS)
            ChronoUnit.MILLENNIA -> minus(value * YEARS_PER_MILLENNIUM, ChronoUnit.YEARS)

            else -> throw InvalidTimeUnitException(unit.name)
        }
    }

    /**
     * Get the stored value for a given supported [ChronoUnit], additionally returning 0 if no value is found.
     */
    public fun get(unit: ChronoUnit): Long = if (unit in SUPPORTED_UNITS) {
        values[unit] ?: 0
    } else {
        throw InvalidTimeUnitException(unit.name)
    }

    /**
     * Given a value and [ChronoUnit], replace any stored value for that unit with the given value.
     */
    public fun set(value: Long, unit: ChronoUnit) {
        when (unit) {
            in SUPPORTED_UNITS -> values[unit] = value

            ChronoUnit.WEEKS -> set(value * DAYS_PER_WEEK, ChronoUnit.DAYS)
            ChronoUnit.DECADES -> set(value * YEARS_PER_DECADE, ChronoUnit.YEARS)
            ChronoUnit.CENTURIES -> set(value * YEARS_PER_CENTURY, ChronoUnit.YEARS)
            ChronoUnit.MILLENNIA -> set(value * YEARS_PER_MILLENNIUM, ChronoUnit.YEARS)

            else -> throw InvalidTimeUnitException(unit.name)
        }
    }

    /**
     * Given a [LocalDateTime] (defaulting to `now()`), normalize the stored values by applying them to the datetime
     * and calculating their real-world difference. This will replace all stored values with newly-normalized values.
     */
    public fun normalize(dateTime: LocalDateTime = LocalDateTime.now()) {
        var newDateTime = dateTime

        values.forEach { unit, value ->
            newDateTime = newDateTime.plus(value, unit)
        }

        val nowTime = LocalDateTime.now()
        val futureTime = nowTime.plusHours(get(ChronoUnit.HOURS) % HOURS_PER_DAY)
            .plusMinutes(get(ChronoUnit.MINUTES) % MINUTES_PER_HOUR)
            .plusSeconds(get(ChronoUnit.SECONDS) % SECONDS_PER_MINUTE)

        val timePeriodDelta = Period.between(nowTime.toLocalDate(), futureTime.toLocalDate())

        val duration = Duration.between(dateTime, newDateTime)
        val period = Period.between(dateTime.toLocalDate(), newDateTime.toLocalDate())
            .minusDays(timePeriodDelta.days.toLong())

        copyFrom(duration, period)
    }

    /**
     * Given a [Temporal] subclass, apply the values in this container to it and return the result.
     *
     * This function will skip units that the [Temporal] doesn't support.
     */
    public fun <T : Temporal> apply(target: T): T {
        var result = target

        values.forEach { unit, value ->
            if (target.isSupported(unit)) {
                result = target.plus(value, unit) as T
            }
        }

        return result
    }

    /**
     * Given a [Duration] and [Period], replace all values in this container with values contained within the passed
     * objects.
     */
    public fun copyFrom(duration: Duration, period: Period) {
        SUPPORTED_UNITS.forEach { unit ->
            when (unit) {
                ChronoUnit.SECONDS -> values[unit] = duration.toSecondsPart().toLong()
                ChronoUnit.MINUTES -> values[unit] = duration.toMinutesPart().toLong()
                ChronoUnit.HOURS -> values[unit] = duration.toHoursPart().toLong()

                ChronoUnit.DAYS -> values[unit] = period.days.toLong()
                ChronoUnit.MONTHS -> values[unit] = period.months.toLong()
                ChronoUnit.YEARS -> values[unit] = period.years.toLong()
            }
        }
    }

    /**
     * Given a [Duration], replace all time-relevant values with values contained within the passed object.
     */
    public fun copyFrom(duration: Duration) {
        SUPPORTED_UNITS.forEach { unit ->
            when (unit) {
                ChronoUnit.SECONDS -> values[unit] = duration.toSecondsPart().toLong()
                ChronoUnit.MINUTES -> values[unit] = duration.toMinutesPart().toLong()
                ChronoUnit.HOURS -> values[unit] = duration.toHoursPart().toLong()
            }
        }
    }

    /**
     * Given a [Period], replace all date-relevant values with values contained within the passed object.
     */
    public fun copyFrom(period: Period) {
        SUPPORTED_UNITS.forEach { unit ->
            when (unit) {
                ChronoUnit.DAYS -> values[unit] = period.days.toLong()
                ChronoUnit.MONTHS -> values[unit] = period.months.toLong()
                ChronoUnit.YEARS -> values[unit] = period.years.toLong()
            }
        }
    }

    public companion object {
        /**
         * Create a ChronoUnit and populate it with the values contained in the passed [Duration] and [Period] objects.
         */
        public fun of(duration: Duration, period: Period): ChronoContainer {
            val container = ChronoContainer()

            container.copyFrom(duration, period)

            return container
        }

        /**
         * Create a ChronoUnit and populate it with the values contained in the passed [Duration] object.
         */
        public fun of(duration: Duration): ChronoContainer {
            val container = ChronoContainer()

            container.copyFrom(duration)

            return container
        }

        /**
         * Create a ChronoUnit and populate it with the values contained in the passed [Period] object.
         */
        public fun of(period: Period): ChronoContainer {
            val container = ChronoContainer()

            container.copyFrom(period)

            return container
        }

        /**
         * Given two [LocalDateTime] objects, calculate the difference between them and return a new ChronoUnit,
         * pre-populated with that difference.
         */
        public fun between(before: LocalDateTime, after: LocalDateTime): ChronoContainer {
            val duration = Duration.between(before, after)
            val period = Period.between(before.toLocalDate(), after.toLocalDate())

            return of(duration, period)
        }

        /**
         * Given two [LocalDate] objects, calculate the difference between them and return a new ChronoUnit,
         * pre-populated with that difference.
         */
        public fun between(before: LocalDate, after: LocalDate): ChronoContainer {
            val period = Period.between(before, after)

            return of(period)
        }

        /**
         * Given two [LocalTime] objects, calculate the difference between them and return a new ChronoUnit,
         * pre-populated with that difference.
         */
        public fun between(before: LocalTime, after: LocalTime): ChronoContainer {
            val duration = Duration.between(before, after)

            return of(duration)
        }
    }
}
