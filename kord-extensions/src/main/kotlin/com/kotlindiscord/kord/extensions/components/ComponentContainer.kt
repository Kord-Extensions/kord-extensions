@file:Suppress("AnnotationSpacing")  // Genuinely hate having to deal with this one sometimes.

package com.kotlindiscord.kord.extensions.components

import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** The maximum number of slots you can have in a row. **/
public const val ROW_SIZE: Int = 5

/** Class representing a single set of components that can be applied to any message. **/
public open class ComponentContainer : KoinComponent {
    internal val registry: ComponentRegistry by inject()

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

    /** Remove all components, and unregister them from the [ComponentRegistry]. **/
    public open fun removeAll() {
        rows.toList().flatten().forEach { component ->
            if (component is ComponentWithID) {
                registry.unregister(component)
            }
        }

        rows.forEach { it.clear() }
    }

    /** Remove the given component, and unregister it from the [ComponentRegistry]. **/
    public open fun remove(component: Component): Boolean {
        if (rows.any { it.remove(component) }) {
            if (component is ComponentWithID) {
                registry.unregister(component)
            }

            return true
        }

        return false
    }

    /** Given two components, replace the old component with the new one and likewise handle registration. **/
    public open fun replace(old: Component, new: Component): Boolean {
        for (row in rows) {
            val index = row.indexOf(old)

            if (index == -1) {
                continue
            }

            @Suppress("UnnecessaryParentheses")  // Yeah, but let me be paranoid. Please.
            val freeSlots = (ROW_SIZE - row.size) + old.unitWidth

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
    public open fun replace(id: String, new: Component): Boolean {
        for (row in rows) {
            val index = row.indexOfFirst { it is ComponentWithID && it.id == id }

            if (index == -1) {
                continue
            }

            val old = row[index]
            val freeSlots = old.unitWidth + (ROW_SIZE - row.size)

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
    public open fun add(component: Component, rowNum: Int? = null) {
        component.validate()

        if (rowNum == null) {
            unsortedComponents.add(component)

            return
        }

        if (rowNum < 0 || rowNum >= rows.size) {
            error("The given row number ($rowNum) must be between 0 to ${rows.size - 1}, inclusive.")
        }

        val row = rows[rowNum]

        if (row.size >= ROW_SIZE) {
            error(
                "Row $rowNum is full, no more components can be added to it."
            )
        }

        if (row.size + component.unitWidth > ROW_SIZE) {
            error(
                "The given component takes up ${component.unitWidth} slots, but row $rowNum only has " +
                    "${ROW_SIZE - row.size} available slots remaining."
            )
        }

        row.add(component)

        if (component is ComponentWithID) {
            registry.register(component)
        }
    }

    /** Sort all components in [unsortedComponents] by packing them into rows as tightly as possible. **/
    public open fun sort() {
        while (unsortedComponents.isNotEmpty()) {
            val component = unsortedComponents.removeFirst()
            var sorted = false

            @Suppress("UnconditionalJumpStatementInLoop")  // Yes, but this is nicer to read
            for (row in rows) {
                if (row.size >= ROW_SIZE || row.size + component.unitWidth > ROW_SIZE) {
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

    /** Apply the components in this container to a message that's being created. **/
    public open fun MessageCreateBuilder.applyToMessage() {
        sort()

        for (row in rows.filter { it.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }
    }

    /** Apply the components in this container to a message that's being edited. **/
    public open fun MessageModifyBuilder.applyToMessage() {
        sort()

        for (row in rows.filter { it.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }
    }
}

/** DSL-style factory function to make component containers these by hand easier. **/
@Suppress("FunctionNaming")  // It's a factory function, detekt...
public suspend fun ComponentContainer(builder: suspend ComponentContainer.() -> Unit): ComponentContainer {
    val container = ComponentContainer()

    builder(container)

    return container
}
