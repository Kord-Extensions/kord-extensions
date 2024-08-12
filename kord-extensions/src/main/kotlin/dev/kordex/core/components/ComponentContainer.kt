/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("AnnotationSpacing")

// Genuinely hate having to deal with this one sometimes.

package dev.kordex.core.components

import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.scheduling.Task
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject
import kotlin.time.Duration

/** The maximum number of slots you can have in a row. **/
public const val ROW_SIZE: Int = 5

/**
 * Class representing a single set of components that can be applied to any message.
 *
 * A timeout is supported by this class. If a [timeout] is provided, the components stored within this container will
 * be unregistered from the [ComponentRegistry]. If this container contains actionable components, the timeout will be
 * reset whenever an actionable component is interacted with.
 *
 * The `startTimeoutNow` parameter defaults to `false`, but will be set to `true` automatically if you're using a
 * `components` DSL function provided in the message creation/modification builders. When this is `true`, the timeout
 * task will be started immediately - when it's `false`, you'll have to call the [timeoutTask] `start` function
 * yourself.
 *
 * @param timeout Optional timeout duration.
 * @param startTimeoutNow Whether to start the timeout immediately.
 */
public open class ComponentContainer(
	public val timeout: Duration? = null,
	startTimeoutNow: Boolean = false,
) : KordExKoinComponent {
	internal val registry: ComponentRegistry by inject()

	/** If a [timeout] was provided, the scheduled timeout task will be stored here. **/
	public open val timeoutTask: Task? = if (timeout != null) {
		runBlocking { // This is a trivially quick block, so it should be fine.
			registry.scheduler.schedule(timeout, startNow = startTimeoutNow) {
				removeAll()

				timeoutCallback?.invoke(this@ComponentContainer)
			}
		}
	} else {
		null
	}

	/** Extra callback to run when this container times out, if any. **/
	public open var timeoutCallback: (suspend (ComponentContainer).() -> Unit)? = null

	/** Components that haven't been sorted into rows by [sort] yet. **/
	public open val unsortedComponents: MutableList<Component> = mutableListOf()

	/** Array containing sorted rows of components. **/
	public open val rows: Array<MutableList<Component>> = arrayOf(
		// Up to 5 rows of components

		mutableListOf(),
		mutableListOf(),
		mutableListOf(),
		mutableListOf(),
		mutableListOf(),
	)

	/** Register an additional callback to be run when this container times out, assuming a timeout is configured. **/
	public open fun onTimeout(callback: suspend (ComponentContainer).() -> Unit) {
		timeoutCallback = callback
	}

	/** Remove all components, and unregister them from the [ComponentRegistry]. **/
	public open suspend fun removeAll() {
		rows.toList().flatten().forEach { component ->
			if (component is ComponentWithID) {
				registry.unregister(component)
			}
		}

		rows.forEach { it.clear() }
	}

	/** Remove the given component, and unregister it from the [ComponentRegistry]. **/
	public open suspend fun remove(component: Component): Boolean {
		if (rows.any { it.remove(component) }) {
			if (component is ComponentWithID) {
				registry.unregister(component)
			}

			return true
		}

		return false
	}

	/** Given two components, replace the old component with the new one and likewise handle registration. **/
	public open suspend fun replace(old: Component, new: Component): Boolean {
		for (row in rows) {
			val index = row.indexOf(old)

			if (index == -1) {
				continue
			}

			@Suppress("UnnecessaryParentheses")  // Yeah, but let me be paranoid. Please.
			val freeSlots = (ROW_SIZE - rowWidth(row)) + old.unitWidth

			if (new.unitWidth > freeSlots) {
				error(
					"The given component takes up ${old.unitWidth} slot/s, but its row will only have " +
						"$freeSlots available slots remaining."
				)
			}

			row[index] = new

			if (new is ComponentWithID) {
				registry.register(new)
			}

			return true
		}

		return false
	}

	/**
	 * Given an old component ID and new component, replace the old component with the new one and likewise handle
	 * registration.
	 */
	public open suspend fun replace(id: String, new: Component): Boolean {
		for (row in rows) {
			val index = row.indexOfFirst { it is ComponentWithID && it.id == id }

			if (index == -1) {
				continue
			}

			val old = row[index]
			val freeSlots = old.unitWidth + (ROW_SIZE - rowWidth(row))

			if (new.unitWidth > freeSlots) {
				error(
					"The given component takes up ${old.unitWidth} slots, but its row will only have " +
						"$freeSlots available slots remaining."
				)
			}

			row[index] = new

			if (new is ComponentWithID) {
				registry.register(new)
			}

			return true
		}

		return false
	}

	/**
	 *  Add a component. New components will be unsorted, or placed in the numbered row denoted by [rowNum] if
	 *  possible.
	 */
	public open suspend fun add(component: Component, rowNum: Int? = null) {
		component.validate()

		if (rowNum == null) {
			unsortedComponents.add(component)

			return
		}

		if (rowNum < 0 || rowNum >= rows.size) {
			error("The given row number ($rowNum) must be between 0 to ${rows.size - 1}, inclusive.")
		}

		val row = rows[rowNum]

		if (rowWidth(row) >= ROW_SIZE) {
			error(
				"Row $rowNum is full, no more components can be added to it."
			)
		}

		if (rowWidth(row) + component.unitWidth > ROW_SIZE) {
			error(
				"The given component takes up ${component.unitWidth} slots, but row $rowNum only has " +
					"${ROW_SIZE - rowWidth(row)} available slots remaining."
			)
		}

		row.add(component)

		if (component is ComponentWithID) {
			registry.register(component)
		}
	}

	/** Sort all components in [unsortedComponents] by packing them into rows as tightly as possible. **/
	public open suspend fun sort() {
		while (unsortedComponents.isNotEmpty()) {
			val component = unsortedComponents.removeFirst()
			var sorted = false

			@Suppress("UnconditionalJumpStatementInLoop")  // Yes, but this is nicer to read
			for (row in rows) {
				if (rowWidth(row) >= ROW_SIZE || rowWidth(row) + component.unitWidth > ROW_SIZE) {
					continue
				}

				row.add(component)
				sorted = true

				break
			}

			if (!sorted) {
				error(
					"Failed to sort components: Couldn't find a row with ${component.unitWidth} empty slots to fit" +
						"$component."
				)
			}

			if (component is ComponentWithID) {
				registry.register(component)
			}
		}
	}

	/** Apply the components in this container to a message that's being created or edited. **/
	public open suspend fun MessageBuilder.applyToMessage() {
		sort()

		for (row in rows.filter { it.isNotEmpty() }) {
			actionRow {
				row.forEach { it.apply(this) }
			}
		}
	}

	/**
	 * Cancel the timeout task, and remove all components from this component container.
	 *
	 * This is equivalent to timing out this container early, but will not run the supplied [timeoutCallback], if one
	 * was provided in [onTimeout].
	 */
	public open suspend fun cancel() {
		timeoutTask?.cancel()
		removeAll()
	}

	private fun rowWidth(row: List<Component>): Int = row.sumOf { it.unitWidth }
}

/** DSL-style factory function to make component containers these by hand easier. **/
@Suppress("FunctionNaming")  // It's a factory function, detekt...
public suspend fun ComponentContainer(
	timeout: Duration? = null,
	startTimeoutNow: Boolean = false,
	builder: suspend ComponentContainer.() -> Unit,
): ComponentContainer {
	val container = ComponentContainer(timeout, startTimeoutNow)

	builder(container)

	return container
}
