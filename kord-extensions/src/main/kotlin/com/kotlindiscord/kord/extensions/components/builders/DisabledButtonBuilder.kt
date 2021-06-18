@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder
import java.util.*

/**
 * Button builder representing a disabled interactive button. No click action.
 *
 * Either a [label] or [emoji] must be provided. [style] must not be [ButtonStyle.Link].
 */
public open class DisabledButtonBuilder : ButtonBuilder() {
    /** Unique ID for this button. Required by Discord. **/
    public open var id: String = UUID.randomUUID().toString()

    /** Button style. **/
    public open var style: ButtonStyle = ButtonStyle.Primary

    public override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji
            label = this@DisabledButtonBuilder.label

            disabled = true
        }
    }

    public override fun validate() {
        if (label == null && partialEmoji == null) {
            error("Disabled buttons must have either a label or emoji.")
        }

        if (style == ButtonStyle.Link) {
            error("The Link button style is reserved for link buttons.")
        }
    }
}
