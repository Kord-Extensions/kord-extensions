@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.commands.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.components.builders.ButtonBuilder
import com.kotlindiscord.kord.extensions.components.builders.DisabledButtonBuilder
import com.kotlindiscord.kord.extensions.components.builders.InteractiveButtonBuilder
import com.kotlindiscord.kord.extensions.components.builders.LinkButtonBuilder
import com.kotlindiscord.kord.extensions.events.EventHandler
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.entity.interaction.ComponentInteraction
import dev.kord.core.event.interaction.InteractionCreateEvent
import dev.kord.rest.builder.interaction.FollowupMessageBuilder
import dev.kord.rest.builder.interaction.actionRow
import dev.kord.rest.builder.message.MessageCreateBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Class in charge of keeping track of sets of components, organising them into rows and handling their click
 * actions.
 *
 * Row specification is optional - by default, this class will try to sort buttons into rows automatically, filling
 * in any available spots from top to bottom. If you don't want this, you can specify the row number and the button
 * will be placed at the end of the row.
 *
 * When [parentContext] is provided, interactive button behaviour will match the slash command's current ack type.]
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
    /** Current Kord instance. **/
    public val kord: Kord by inject()

    /** Current event handler instance waiting for interaction creation events. **/
    public var eventHandler: EventHandler<InteractionCreateEvent>? = null

    /** List of buttons that have yet to be sorted into rows. **/
    public open val unsortedButtons: MutableList<ButtonBuilder> = mutableListOf()

    /** Mapping of UUID to interactive button builder, used for handling click actions. **/
    public open val interactiveActions: MutableMap<String, InteractiveButtonBuilder> = mutableMapOf()

    /** Predefined row structure, a 5x5 2D array of nulls. Filled in with button builders later. **/
    public open val rows: Array<Array<ButtonBuilder?>> = arrayOf(
        // Up to 5 rows of 5 buttons each

        arrayOf(null, null, null, null, null),
        arrayOf(null, null, null, null, null),
        arrayOf(null, null, null, null, null),
        arrayOf(null, null, null, null, null),
        arrayOf(null, null, null, null, null),
    )

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

        builder.invoke(buttonBuilder)
        addButton(buttonBuilder, row)

        interactiveActions[buttonBuilder.id] = buttonBuilder

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
        addButton(buttonBuilder, row)

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
        addButton(buttonBuilder, row)

        return buttonBuilder
    }

    /**
     * @suppress Internal API function for validating the given row number and storing the button builder.
     */
    public open fun addButton(builder: ButtonBuilder, row: Int? = null) {
        builder.validate()

        if (row == null) {
            unsortedButtons.add(builder)
        } else {
            if (row < 0 || row >= rows.size) {
                error("The given row number ($row) is invalid - it must be between 0 - ${rows.size - 1} inclusive.")
            }

            val rowArray = rows[row]
            val index = rowArray.indexOfFirst { it == null }

            if (index == -1) {
                error("Row $row is full - up to ${rowArray.size} buttons are allowed per row.")
            }

            rowArray[index] = builder
        }
    }

    /**
     * @suppress Internal API function that tries to sort buttons into rows as tightly as possible.
     */
    public open fun sortIntoRows() {
        while (unsortedButtons.isNotEmpty()) {
            val button = unsortedButtons.removeFirst()

            @Suppress("MagicNumber")
            val row = rows.filter { it.contains(null) }.firstOrNull() ?: error(
                "Failed to sort buttons into rows - there are ${unsortedButtons.size + 26} buttons, but only 25 (5 " +
                    "rows of 5 buttons) are allowed."
            )

            val index = row.indexOfFirst { it == null }

            row[index] = button
        }
    }

    /**
     * @suppress Internal API function that creates an event handler to listen for button presses, with a timeout.
     */
    @Suppress("MagicNumber")  // Turning seconds into millis, again
    public open suspend fun startListening(timeoutSeconds: Long? = null) {
        val timeoutMillis = timeoutSeconds?.let { it * 1000 }

        eventHandler = extension.event {
            check {
                val interaction = it.interaction as? ComponentInteraction

                interaction != null && interaction.componentId in interactiveActions
            }

            action {
                val interaction = event.interaction as ComponentInteraction
                val button = interactiveActions[interaction.componentId]!!

                button.call(this@Components, extension, event, parentContext)
            }
        }

        if (timeoutMillis != null) {
            kord.launch {
                delay(timeoutMillis)

                stop()
            }
        }
    }

    /**
     * Stop listening for interaction events. Interactive buttons will no longer function.
     */
    public open fun stop() {
        eventHandler?.job?.cancel()
    }

    /**
     * @suppress Internal API function that sets up all of the buttons, adds them to the message, and listens for
     * clicks.
     */
    public open suspend fun MessageCreateBuilder.setup(timeoutSeconds: Long? = null) {
        sortIntoRows()

        for (row in rows) {
            actionRow {
                row.forEach { it?.apply(this) }
            }
        }

        startListening(timeoutSeconds)
    }

    /**
     * @suppress Internal API function that sets up all of the buttons, adds them to the message, and listens for
     * clicks.
     */
    public open suspend fun FollowupMessageBuilder<*>.setup(timeoutSeconds: Long? = null) {
        sortIntoRows()

        for (row in rows.filter { it.count { it == null } != it.count() }) {
            actionRow {
                row.filterNotNull().forEach { it.apply(this) }
            }
        }

        startListening(timeoutSeconds)
    }
}
