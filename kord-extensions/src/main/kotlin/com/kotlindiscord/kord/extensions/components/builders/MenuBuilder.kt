@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.components.builders

import com.kotlindiscord.kord.extensions.components.Components
import com.kotlindiscord.kord.extensions.components.contexts.MenuContext
import com.kotlindiscord.kord.extensions.extensions.Extension
import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.entity.interaction.SelectMenuInteraction
import dev.kord.core.event.interaction.ComponentCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder

private const val DESCRIPTION_MAX = 50
private const val LABEL_MAX = 25
private const val OPTIONS_MAX = 25
private const val PLACEHOLDER_MAX = 100
private const val VALUE_MAX = 100

/**
 * Builder representing a dropdown menu on Discord.
 *
 * At least one option must be provided.
 */
public open class MenuBuilder : ActionableComponentBuilder<SelectMenuInteraction, MenuContext>() {
    /** List of options for the user to choose from. **/
    public val options: MutableList<SelectOptionBuilder> = mutableListOf()

    /** The minimum number of choices that the user must make. **/
    public var minimumChoices: Int = 1

    /** The maximum number of choices that the user can make. Set to `null` for no maximum. **/
    public var maximumChoices: Int? = 1

    /** Placeholder text to show before the user has selected any options.. **/
    public var placeholder: String? = null

    // Menus can only be on their own in a row.
    override val rowExclusive: Boolean = true

    /** Add an option to this select menu. **/
    public suspend fun option(label: String, value: String, body: suspend SelectOptionBuilder.() -> Unit = {}) {
        val builder = SelectOptionBuilder(label, value)

        body(builder)

        if (builder.description?.length ?: 0 > DESCRIPTION_MAX) {
            error("Option descriptions must not be longer than $DESCRIPTION_MAX characters.")
        }

        if (builder.label.length > LABEL_MAX) {
            error("Option labels must not be longer than $LABEL_MAX characters.")
        }

        if (builder.value.length > VALUE_MAX) {
            error("Option values must not be longer than $VALUE_MAX characters.")
        }

        options.add(builder)
    }

    public override fun apply(builder: ActionRowBuilder) {
        if (maximumChoices == null || maximumChoices!! > options.size) {
            maximumChoices = options.size
        }

        builder.selectMenu(id) {
            allowedValues = minimumChoices..maximumChoices!!

            this.options.addAll(this@MenuBuilder.options)
            this.placeholder = this@MenuBuilder.placeholder
        }
    }

    override fun validate() {
        if (this.options.isEmpty()) {
            error("Menu components must have at least one option.")
        }

        if (this.options.size > OPTIONS_MAX) {
            error("Menu components must not have more than $OPTIONS_MAX options.")
        }

        if (this.placeholder?.length ?: 0 > PLACEHOLDER_MAX) {
            error("Menu components must not have a placeholder longer than $PLACEHOLDER_MAX characters.")
        }

        super.validate()
    }

    override fun getContext(
        extension: Extension,
        event: ComponentCreateEvent,
        components: Components,
        interactionResponse: InteractionResponseBehavior?,
        interaction: SelectMenuInteraction
    ): MenuContext = MenuContext(
        extension, event, components, interactionResponse, interaction
    )
}
