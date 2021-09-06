package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.buttons.DisabledInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.LinkInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.menus.EphemeralSelectMenu
import com.kotlindiscord.kord.extensions.components.menus.PublicSelectMenu
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder

public suspend fun ComponentContainer.disabledButton(
    row: Int? = null,
    builder: suspend DisabledInteractionButton.() -> Unit
): DisabledInteractionButton {
    val component = DisabledInteractionButton()

    builder(component)
    add(component, row)

    return component
}

public suspend fun ComponentContainer.ephemeralButton(
    row: Int? = null,
    builder: suspend EphemeralInteractionButton.() -> Unit
): EphemeralInteractionButton {
    val component = EphemeralInteractionButton()

    builder(component)
    add(component, row)

    return component
}

public suspend fun ComponentContainer.linkButton(
    row: Int? = null,
    builder: suspend LinkInteractionButton.() -> Unit
): LinkInteractionButton {
    val component = LinkInteractionButton()

    builder(component)
    add(component, row)

    return component
}

public suspend fun ComponentContainer.publicButton(
    row: Int? = null,
    builder: suspend PublicInteractionButton.() -> Unit
): PublicInteractionButton {
    val component = PublicInteractionButton()

    builder(component)
    add(component, row)

    return component
}

public suspend fun ComponentContainer.ephemeralSelectMenu(
    row: Int? = null,
    builder: suspend EphemeralSelectMenu.() -> Unit
): EphemeralSelectMenu {
    val component = EphemeralSelectMenu()

    builder(component)
    add(component, row)

    return component
}

public suspend fun ComponentContainer.publicSelectMenu(
    row: Int? = null,
    builder: suspend PublicSelectMenu.() -> Unit
): PublicSelectMenu {
    val component = PublicSelectMenu()

    builder(component)
    add(component, row)

    return component
}

public fun MessageCreateBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        applyToMessage()
    }
}

public fun MessageModifyBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        applyToMessage()
    }
}

public suspend fun MessageCreateBuilder.components(builder: suspend ComponentContainer.() -> Unit): ComponentContainer {
    val container = ComponentContainer(builder)

    applyComponents(container)

    return container
}

public suspend fun MessageModifyBuilder.components(builder: suspend ComponentContainer.() -> Unit): ComponentContainer {
    val container = ComponentContainer(builder)

    applyComponents(container)

    return container
}
