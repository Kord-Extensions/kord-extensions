/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms

import com.kotlindiscord.kord.extensions.components.forms.widget.Widget
import com.kotlindiscord.kord.extensions.utils.map


public typealias WidgetGrid = Array<MutableList<Widget>>
public typealias CoordinatePair = Pair<Int, Int>

public const val GRID_WIDTH: Int = 5
public const val GRID_HEIGHT: Int = 5
public const val GRID_CAPACITY: Int = GRID_WIDTH * GRID_HEIGHT


// region: Coordinate Functions

public infix fun Int.x(other: Int): CoordinatePair =
    CoordinatePair(this, other)

public operator fun CoordinatePair.plus(other: CoordinatePair): Pair<Int, Int> =
    (first + other.first) x (second + other.second)

public operator fun CoordinatePair.minus(other: CoordinatePair): Pair<Int, Int> =
    (first - other.first) x (second - other.second)

public fun CoordinatePair.permutationsUpto(end: CoordinatePair): MutableList<CoordinatePair> {
    val values = mutableListOf<CoordinatePair>()

    for (row in first..end.first) {
        for (column in second..end.second) {
            values.add((row x column))
        }
    }

    return values
}

public fun CoordinatePair.permutationsUptoExclusive(end: CoordinatePair): MutableList<CoordinatePair> {
    val values = mutableListOf<CoordinatePair>()

    for (row in first until end.first) {
        for (column in second until end.second) {
            values.add((row x column))
        }
    }

    return values
}

public fun CoordinatePair.forEachUpto(end: CoordinatePair, body: (CoordinatePair) -> Unit) {
    for (row in first..end.first) {
        for (column in second..end.second) {
            body(row x column)
        }
    }
}

public fun CoordinatePair.forEachUptoExclusive(end: CoordinatePair, body: (CoordinatePair) -> Unit) {
    for (row in first until end.first) {
        for (column in second until end.second) {
            body(row x column)
        }
    }
}

public inline fun <T> CoordinatePair.mapUpto(end: CoordinatePair, body: (CoordinatePair) -> T): List<T> {
    val values = mutableListOf<T>()

    for (row in first..end.first) {
        for (column in second..end.second) {
            values.add(body(row x column))
        }
    }

    return values
}

public inline fun <T> CoordinatePair.mapUptoExclusive(end: CoordinatePair, body: (CoordinatePair) -> T): List<T> {
    val values = mutableListOf<T>()

    for (row in first until end.first) {
        for (column in second until end.second) {
            values.add(body(row x column))
        }
    }

    return values
}

public inline fun <T> CoordinatePair.mapNotNullUpto(end: CoordinatePair, body: (CoordinatePair) -> T?): List<T> {
    val values = mutableListOf<T>()

    for (row in first..end.first) {
        for (column in second..end.second) {
            val result = body(row x column)
                ?: continue

            values.add(result)
        }
    }

    return values
}

public inline fun <T> CoordinatePair.mapNotNullUptoExclusive(
    end: CoordinatePair,
    body: (CoordinatePair) -> T?,
): List<T> {
    val values = mutableListOf<T>()

    for (row in first until end.first) {
        for (column in second until end.second) {
            val result = body(row x column)
                ?: continue

            values.add(result)
        }
    }

    return values
}

public fun CoordinatePair.isValid(): Boolean =
    first in 0 until GRID_WIDTH
        && second in 0 until GRID_HEIGHT

public fun CoordinatePair.throwIfInvalid(name: String = "Coordinate") {
    if (!isValid()) {
        error("$name row and column must be within 0 <= x < $GRID_HEIGHT; got $this")
    }
}

public fun CoordinatePair.toString(): String =
    "($first, $second)"

// endregion

// region: Grid functions

public fun WidgetGrid(): WidgetGrid = arrayOf(
    *GRID_HEIGHT.map { mutableListOf<Widget>() }.toTypedArray()
)

public fun WidgetGrid.get(coordinate: CoordinatePair): Widget? {
    coordinate.throwIfInvalid()

    return this[coordinate.first].getOrNull(coordinate.second)
}

public fun WidgetGrid.set(coordinate: CoordinatePair, widget: Widget) {
    coordinate.throwIfInvalid("Start coordinate")

    val end = coordinate + (widget.width x widget.height)

    end.throwIfInvalid("End coordinate")

    val permutations = coordinate.permutationsUpto(end)
    val existing = permutations.mapNotNull { get(it) }.toSet()

    if (existing.isNotEmpty()) {
        error("Can't add widget from $coordinate to $end as it would overlap ${existing.size} other widgets")
    }

    permutations.forEach {
        this[it.first][it.second] = widget
    }
}

public fun WidgetGrid.removeAt(coordinate: CoordinatePair): Boolean =
    if (get(coordinate) != null) {
        this[coordinate.first].removeAt(coordinate.second)

        true
    } else {
        false
    }


public fun WidgetGrid.remove(widget: Widget): Boolean {
    val coordinates = coordinatesFor(widget)

    if (coordinates.isEmpty()) {
        return false
    }

    coordinates.map(::removeAt)

    return true
}

public fun WidgetGrid.coordinatesFor(widget: Widget): MutableList<CoordinatePair> {
    val values = mutableListOf<CoordinatePair>()

    forEachIndexed { rowIndex, row ->
        row.forEachIndexed { columnIndex, storedWidget ->
            if (widget === storedWidget) {
                values.add(rowIndex to columnIndex)
            }
        }
    }

    return values
}

// endregion
