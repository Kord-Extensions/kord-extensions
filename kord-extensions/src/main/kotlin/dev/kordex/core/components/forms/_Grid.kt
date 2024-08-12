/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UndocumentedPublicProperty", "UndocumentedPublicFunction")

package dev.kordex.core.components.forms

import dev.kordex.core.components.forms.widgets.Widget
import dev.kordex.core.utils.map

public typealias WidgetGrid = Array<MutableList<Widget<*>?>>

/** A coordinate pair, represented by a `Pair(row, column)`. **/
public typealias CoordinatePair = Pair<Int, Int>

public const val GRID_WIDTH: Int = 5
public const val GRID_HEIGHT: Int = 5
public const val GRID_CAPACITY: Int = GRID_WIDTH * GRID_HEIGHT

// region: Coordinate Functions

public infix fun Int.x(other: Int): CoordinatePair =
	CoordinatePair(this, other)

@Suppress("UnnecessaryParentheses")
public operator fun CoordinatePair.plus(other: CoordinatePair): Pair<Int, Int> =
	(first + other.first) x (second + other.second)

@Suppress("UnnecessaryParentheses")
public operator fun CoordinatePair.minus(other: CoordinatePair): Pair<Int, Int> =
	(first - other.first) x (second - other.second)

public fun CoordinatePair.permutationsUpto(end: CoordinatePair): MutableList<CoordinatePair> {
	val values = mutableListOf<CoordinatePair>()

	for (row in first..end.first) {
		for (column in second..end.second) {
			values.add(row x column)
		}
	}

	return values
}

public fun CoordinatePair.permutationsUptoExclusive(end: CoordinatePair): MutableList<CoordinatePair> {
	val values = mutableListOf<CoordinatePair>()

	for (row in first until end.first) {
		for (column in second until end.second) {
			values.add(row x column)
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
	first in 0 until GRID_HEIGHT &&
		second in 0 until GRID_WIDTH

public fun CoordinatePair.throwIfInvalid(name: String = "Coordinate") {
	if (!isValid()) {
		error("$name row and column must be within 0 <= x < $GRID_HEIGHT; got $this")
	}
}

public fun CoordinatePair.toString(): String =
	"($first x $second)"

// endregion

// region: Grid functions

@Suppress("MagicNumber")
public fun WidgetGrid.toString(): String = buildString {
	val grid = this@toString

	appendLine("WidgetGrid@${hashCode()} -> ")

	append(
		grid.joinToString(",\n") { row ->
			val rowString = row.joinToString(" | ") { widget ->
				if (widget == null) {
					"null"
				} else {
					widget::class.simpleName ?: "null"
				}.padEnd(20)
			}

			"  | $rowString |"
		}
	)
}

@Suppress("USELESS_CAST", "SpreadOperator")  // You're just wrong here, IJ
public fun WidgetGrid(): WidgetGrid = arrayOf(
	*GRID_HEIGHT.map {
		GRID_WIDTH.map { null as Widget<*>? }.toMutableList()
	}.toTypedArray()
)

public fun WidgetGrid.get(coordinate: CoordinatePair): Widget<*>? {
	coordinate.throwIfInvalid()

	return this[coordinate.first].getOrNull(coordinate.second)
}

@Suppress("UnnecessaryParentheses")
public fun WidgetGrid.setAtCoordinateOrFirstRow(coordinate: CoordinatePair?, widget: Widget<*>) {
	if (coordinate == null) {
		val row = indexOfFirst { (GRID_WIDTH - it.count { e -> e != null }) >= widget.width }

		if (row == -1) {
			error("No rows are available to fit this widget into.")
		}

		val column = get(row).indexOf(null)

		set(row x column, widget)
	} else {
		set(coordinate, widget)
	}
}

public fun WidgetGrid.set(coordinate: CoordinatePair, widget: Widget<*>) {
	coordinate.throwIfInvalid("Start coordinate")

	val end = coordinate + (widget.height - 1 x widget.width - 1)

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

public fun WidgetGrid.remove(widget: Widget<*>): Boolean {
	val coordinates = coordinatesFor(widget)

	if (coordinates.isEmpty()) {
		return false
	}

	coordinates.map(::removeAt)

	return true
}

public fun WidgetGrid.coordinatesFor(widget: Widget<*>): List<CoordinatePair> {
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

public fun WidgetGrid.getWidgetSet(): Set<Widget<*>> {
	val values = mutableSetOf<Widget<*>>()

	forEach { row ->
		row.forEach { widget ->
			if (widget != null) {
				values.add(widget)
			}
		}
	}

	return values
}

// endregion
