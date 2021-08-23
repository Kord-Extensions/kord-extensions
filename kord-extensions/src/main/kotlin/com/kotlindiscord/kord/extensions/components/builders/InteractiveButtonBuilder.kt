@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.contexts.InteractiveButtonContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.entity.interaction.ButtonInteraction
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Button builder representing an interactive button, with click action.
 *
 * Either a [label] or [emoji] must be provided. [style] must not be [ButtonStyle.Link].
 */
public open class InteractiveButtonBuilder : ButtonBuilder,
    ActionableComponentBuilder<ButtonInteraction, InteractiveButtonContext>() {
    /** Button style. **/
    public open var style: ButtonStyle = ButtonStyle.Primary

    override var label: String? = null
    override var partialEmoji: DiscordPartialEmoji? = null

    public override fun apply(builder: ActionRowBuilder) {
        builder.interactionButton(style, id) {
            emoji = partialEmoji

            // ZWSP, so iOS users don't have to directly tap an emoji if there's no label
            label = this@InteractiveButtonBuilder.label ?: "\u200B"
        }
    }

    public override fun validate() {
        if (label == null && partialEmoji == null) {
            error("Interactive buttons must have either a label or emoji.")
        }

        if (style == ButtonStyle.Link) {
            error("The Link button style is reserved for link buttons.")
        }

        super.validate()
    }

    override fun getContext(
        extension: Extension,
        event: ComponentInteractionCreateEvent,
        components: Components,
        interactionResponse: InteractionResponseBehavior?,
        interaction: ButtonInteraction
    ): InteractiveButtonContext = InteractiveButtonContext(
        extension, event, components, interactionResponse, interaction
    )
}
