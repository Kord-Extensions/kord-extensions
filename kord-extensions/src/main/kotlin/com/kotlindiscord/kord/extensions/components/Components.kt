@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.commands.slash.AutoAckType
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.builders.*
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.event
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.ComponentCreateEvent
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private const val COMPONENTS_PER_ROW = 5

/**
 * Class in charge of keeping track of sets of components, organising them into rows and handling their actions.
 *
 * Row specification is optional - by default, this class will try to sort components into rows automatically, filling
 * in any available spots from top to bottom. If you don't want this, you can specify the row number and the component
 * will be placed at the end of the row.
 *
 * When [parentContext] is provided, actionable component behaviour will match the slash command's current ack type.
 *
 * You most likely don't want to instantiate this class yourself - check the `components` DSL function available in
 * all message creation contexts instead.
 *
 * @param extension Extension object parenting this `Components` instance.
 * @param parentContext Optionally, parent slash command context.
 */
public open class Components(
    public open val extension: Extension,
    public open val parentContext: SlashCommandContext<*>? = null
) : KoinComponent {
    private val logger = KotlinLogging.logger {}

    /** Current Kord instance. **/
    public val kord: Kord by inject()

    /** Current event handler instance waiting for interaction creation events. **/
    public var eventHandler: EventHandler<ComponentCreateEvent>? = null

    /** @suppress Internal Job object representing the timeout job. **/
    public var delayJob: Job? = null

    /** List of components that have yet to be sorted into rows. **/
    public open val unsortedComponents: MutableList<ComponentBuilder> = mutableListOf()

    /** Mapping of UUID to actionable component builder, used for handling interactions. **/
    public open val actionableComponents: MutableMap<String, ActionableComponentBuilder<*, *>> = mutableMapOf()

    /** Predefined row structure, a 5x5 2D array of nulls. Filled in with component builders later. **/
    public open val rows: Array<MutableList<ComponentBuilder>> = arrayOf(
        // Up to 5 rows of components

        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
        mutableListOf(),
    )

    /** List of registered timeout callbacks. **/
    public open val timeoutCallbacks: MutableList<suspend () -> Unit> = mutableListOf()

    /**
     * Create an interactive button that may be clicked on.
     *
     * @see InteractiveButtonBuilder
     */
    public open suspend fun interactiveButton(
        row: Int? = null,
        builder: suspend InteractiveButtonBuilder.() -> Unit
    ): InteractiveButtonBuilder {
        val buttonBuilder = InteractiveButtonBuilder()

        if (parentContext == null) {
            buttonBuilder.autoAck = AutoAckType.PUBLIC
        }

        builder.invoke(buttonBuilder)
        addComponent(buttonBuilder, row)

        actionableComponents[buttonBuilder.id] = buttonBuilder

        return buttonBuilder
    }

    /**
     * Create a link button that directs users to a URL when clicked.
     *
     * @see LinkButtonBuilder
     */
    public open suspend fun linkButton(
        row: Int? = null,
        builder: suspend LinkButtonBuilder.() -> Unit
    ): LinkButtonBuilder {
        val buttonBuilder = LinkButtonBuilder()

        builder.invoke(buttonBuilder)
        addComponent(buttonBuilder, row)

        return buttonBuilder
    }

    /**
     * Create a disabled interactive button, which does nothing when clicked.
     *
     * @see DisabledButtonBuilder
     */
    public open suspend fun disabledButton(
        row: Int? = null,
        builder: suspend DisabledButtonBuilder.() -> Unit
    ): DisabledButtonBuilder {
        val buttonBuilder = DisabledButtonBuilder()

        builder.invoke(buttonBuilder)
        addComponent(buttonBuilder, row)

        return buttonBuilder
    }

    /**
     * Create a dropdown menu, allowing users to select one or more values.
     *
     * @see MenuBuilder
     */
    public open suspend fun menu(
        row: Int? = null,
        builder: suspend MenuBuilder.() -> Unit
    ): MenuBuilder {
        val menuBuilder = MenuBuilder()

        builder.invoke(menuBuilder)
        addComponent(menuBuilder, row)

        actionableComponents[menuBuilder.id] = menuBuilder

        return menuBuilder
    }

    /** Register a callback to run when a setup timeout expires. **/
    public open fun onTimeout(body: suspend () -> Unit): Boolean =
        timeoutCallbacks.add(body)

    /** @suppress Internal API function that runs the timeout callbacks. **/
    @Suppress("TooGenericExceptionCaught")
    public open suspend fun runTimeoutCallbacks() {
        timeoutCallbacks.forEach {
            try {
                it()
            } catch (t: Throwable) {
                logger.error(t) { "Error during timeout callback: $it" }
            }
        }
    }

    /**
     * @suppress Internal API function for validating the given row number and storing the component builder.
     */
    public open fun addComponent(builder: ComponentBuilder, row: Int? = null) {
        builder.validate()

        if (row == null) {
            unsortedComponents.add(builder)
        } else {
            if (row < 0 || row >= rows.size) {
                error("The given row number ($row) is invalid - it must be between 0 - ${rows.size - 1}, inclusive.")
            }

            val components = rows[row]

            if (components.size >= COMPONENTS_PER_ROW) {
                error(
                    "Row $row is full - up to $COMPONENTS_PER_ROW components are allowed per row, or 1 " +
                        "row-exclusive component."
                )
            }

            if (components.isNotEmpty() && components.any { it.rowExclusive }) {
                error("Row $row contains a row-exclusive component, and can't contain any other components.")
            }

            components.add(builder)
        }
    }

    /**
     * @suppress Internal API function that tries to sort components into rows as tightly as possible.
     */
    public open fun sortIntoRows() {
        while (unsortedComponents.isNotEmpty()) {
            val component = unsortedComponents.removeFirst()

            val row = if (component.rowExclusive) {
                rows.firstOrNull { it.isEmpty() } ?: error(
                    "Failed to sort components into rows - Component $component is row-exclusive, but there are no " +
                        "empty rows left."
                )
            } else {
                rows.firstOrNull { it.size < COMPONENTS_PER_ROW && !it.any { e -> e.rowExclusive } } ?: error(
                    "Failed to sort components into rows - all possible rows are full."
                )
            }

            row.add(component)
        }
    }

    /**
     * @suppress Internal API function that creates an event handler to listen for component interactions, with a
     * timeout.
     */
    @Suppress("MagicNumber")  // Turning seconds into millis, again
    public open suspend fun startListening(timeoutSeconds: Long? = null) {
        val timeoutMillis = timeoutSeconds?.let { it * 1000 }

        eventHandler = extension.event {
            booleanCheck {
                val interaction = it.interaction as? ComponentInteraction

                interaction != null && interaction.componentId in actionableComponents
            }

            action {
                val interaction = event.interaction
                val component = actionableComponents[interaction.componentId]!!

                component.call(this@Components, extension, event, parentContext)
            }
        }

        if (timeoutMillis != null) {
            delayJob = kord.launch {
                delay(timeoutMillis)

                eventHandler?.job?.cancel()
                eventHandler = null

                runTimeoutCallbacks()
                stop()
            }
        }
    }

    /**
     * Stop listening for interaction events. Actionable components will no longer function.
     */
    public open fun stop() {
        eventHandler?.job?.cancel()
        delayJob?.cancel()
    }

    /**
     * @suppress Internal API function that sets up all of the components, adds them to the message, and listens for
     * interactions.
     */
    public open suspend fun MessageCreateBuilder.setup(timeoutSeconds: Long? = null) {
        sortIntoRows()

        for (row in rows.filter { row -> row.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }

        startListening(timeoutSeconds)
    }

    /**
     * @suppress Internal API function that sets up all of the components, adds them to the message, and listens for
     * interactions.
     */
    public open suspend fun MessageModifyBuilder.setup(timeoutSeconds: Long? = null) {
        sortIntoRows()

        for (row in rows.filter { row -> row.isNotEmpty() }) {
            actionRow {
                row.forEach { it.apply(this) }
            }
        }

        startListening(timeoutSeconds)
    }
}
