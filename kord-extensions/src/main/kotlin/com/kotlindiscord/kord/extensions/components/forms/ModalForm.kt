/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.forms

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.components.forms.widgets.LineTextWidget
import com.kotlindiscord.kord.extensions.components.forms.widgets.ParagraphTextWidget
import com.kotlindiscord.kord.extensions.components.forms.widgets.TextInputWidget
import com.kotlindiscord.kord.extensions.components.forms.widgets.Widget
import com.kotlindiscord.kord.extensions.events.ModalInteractionCompleteEvent
import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.rest.builder.interaction.ModalBuilder
import org.koin.core.component.inject
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Class representing a modal form.
 *
 * This should be extended by classes representing individual modals.
 */
public abstract class ModalForm : Form(), KordExKoinComponent {
    /** The modal's title, shown on Discord. **/
    public abstract var title: String

    /** Translation bundle to use for this modal's title and widgets. **/
    public open var bundle: String? = null

    /** @suppress Internal reference to the bot, to submit events to. **/
    protected val bot: ExtensibleBot by inject()

    override val timeout: Duration = 15.minutes

    /** ID representing this modal on Discord. **/
    public var id: String = UUID.randomUUID().toString()

    /** A widget representing a single-line text input. **/
    public fun lineText(
        coordinate: CoordinatePair? = null,
        builder: LineTextWidget.() -> Unit,
    ): LineTextWidget {
        val widget = LineTextWidget()

        builder(widget)
        widget.validate()

        grid.setAtCoordinateOrFirstRow(coordinate, widget)

        return widget
    }

    /** A widget representing a multi-line paragraph input. **/
    public fun paragraphText(
        coordinate: CoordinatePair? = null,
        builder: ParagraphTextWidget.() -> Unit,
    ): ParagraphTextWidget {
        val widget = ParagraphTextWidget()

        builder(widget)
        widget.validate()

        grid.setAtCoordinateOrFirstRow(coordinate, widget)

        return widget
    }

    /** @suppress Internal function called by the component registry. **/
    public suspend fun call(event: ModalSubmitInteractionCreateEvent) {
        grid.filter { it.isNotEmpty() }
            .forEach { row ->
                row.filterNotNull()
                    .forEach { widget ->
                        val textInput = widget as TextInputWidget<*>
                        val value = event.interaction.textInputs[textInput.id]?.value

                        if (value != null) {
                            textInput.setValue(value)
                        }
                    }
            }

        bot.send(
            ModalInteractionCompleteEvent(
                id,
                event.interaction
            )
        )
    }

    /** Given a ModalBuilder, apply this modal's widgets for display on Discord. **/
    public suspend fun applyToBuilder(builder: ModalBuilder, locale: Locale, resolvedBundle: String?) {
        val appliedWidgets = mutableSetOf<Widget<*>>()

        grid.forEach { row ->
            val filteredRow = row.filterNotNull()
                .filter { it !in appliedWidgets }

            if (filteredRow.isNotEmpty()) {
                builder.actionRow {
                    filteredRow.forEach { widget ->
                        if (widget !in appliedWidgets) {
                            widget.apply(this, locale, bundle ?: resolvedBundle)
                            appliedWidgets.add(widget)
                        }
                    }
                }
            }
        }
    }
}
