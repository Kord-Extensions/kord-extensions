package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.buttons.DisabledInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.LinkInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.menus.EphemeralSelectMenu
import com.kotlindiscord.kord.extensions.components.menus.PublicSelectMenu
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder

/** DSL function for creating a disabled button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.disabledButton(
    row: Int? = null,
    builder: suspend DisabledInteractionButton.() -> Unit
): DisabledInteractionButton {
    val component = DisabledInteractionButton()

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating an ephemeral button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralButton(
    row: Int? = null,
    builder: suspend EphemeralInteractionButton.() -> Unit
): EphemeralInteractionButton {
    val component = EphemeralInteractionButton()

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating a link button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.linkButton(
    row: Int? = null,
    builder: suspend LinkInteractionButton.() -> Unit
): LinkInteractionButton {
    val component = LinkInteractionButton()

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating a public button and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicButton(
    row: Int? = null,
    builder: suspend PublicInteractionButton.() -> Unit
): PublicInteractionButton {
    val component = PublicInteractionButton()

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating an ephemeral select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralSelectMenu(
    row: Int? = null,
    builder: suspend EphemeralSelectMenu.() -> Unit
): EphemeralSelectMenu {
    val component = EphemeralSelectMenu()

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating a public select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicSelectMenu(
    row: Int? = null,
    builder: suspend PublicSelectMenu.() -> Unit
): PublicSelectMenu {
    val component = PublicSelectMenu()

    builder(component)
    add(component, row)

    return component
}

/** Convenience function for applying the components in a [ComponentContainer] to a message you're creating. **/
public fun MessageCreateBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        applyToMessage()
    }
}

/** Convenience function for applying the components in a [ComponentContainer] to a message you're editing. **/
public fun MessageModifyBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        applyToMessage()
    }
}

/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * creating.
 */
public suspend fun MessageCreateBuilder.components(builder: suspend ComponentContainer.() -> Unit): ComponentContainer {
    val container = ComponentContainer(builder)

    applyComponents(container)

    return container
}

/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * editing.
 */
public suspend fun MessageModifyBuilder.components(builder: suspend ComponentContainer.() -> Unit): ComponentContainer {
    val container = ComponentContainer(builder)

    applyComponents(container)

    return container
}
