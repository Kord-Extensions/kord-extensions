/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components

import com.kotlindiscord.kord.extensions.components.buttons.DisabledInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.EphemeralInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.LinkInteractionButton
import com.kotlindiscord.kord.extensions.components.buttons.PublicInteractionButton
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.EphemeralSelectMenu
import com.kotlindiscord.kord.extensions.components.menus.PublicSelectMenu
import dev.kord.rest.builder.message.create.MessageCreateBuilder
import dev.kord.rest.builder.message.modify.MessageModifyBuilder
import kotlin.time.Duration

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
    builder: suspend EphemeralInteractionButton<ModalForm>.() -> Unit
): EphemeralInteractionButton<ModalForm> {
    val component = EphemeralInteractionButton<ModalForm>(timeoutTask)

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating an ephemeral button with modal, and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralButton(
    modal: (() -> M)?,
    row: Int? = null,
    builder: suspend EphemeralInteractionButton<M>.() -> Unit
): EphemeralInteractionButton<M> {
    val component = EphemeralInteractionButton(timeoutTask, modal)

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
    builder: suspend PublicInteractionButton<ModalForm>.() -> Unit
): PublicInteractionButton<ModalForm> {
    val component = PublicInteractionButton<ModalForm>(timeoutTask)

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating a public button with modal, and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicButton(
    modal: (() -> M)?,
    row: Int? = null,
    builder: suspend PublicInteractionButton<M>.() -> Unit
): PublicInteractionButton<M> {
    val component = PublicInteractionButton(timeoutTask, modal)

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating an ephemeral select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.ephemeralSelectMenu(
    row: Int? = null,
    builder: suspend EphemeralSelectMenu<ModalForm>.() -> Unit
): EphemeralSelectMenu<ModalForm> {
    val component = EphemeralSelectMenu<ModalForm>(timeoutTask)

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating an ephemeral select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.ephemeralSelectMenu(
    modal: (() -> M)?,
    row: Int? = null,
    builder: suspend EphemeralSelectMenu<M>.() -> Unit
): EphemeralSelectMenu<M> {
    val component = EphemeralSelectMenu<M>(timeoutTask, modal)

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating a public select menu and adding it to the current [ComponentContainer]. **/
public suspend fun ComponentContainer.publicSelectMenu(
    row: Int? = null,
    builder: suspend PublicSelectMenu<ModalForm>.() -> Unit
): PublicSelectMenu<ModalForm> {
    val component = PublicSelectMenu<ModalForm>(timeoutTask)

    builder(component)
    add(component, row)

    return component
}

/** DSL function for creating a public select menu and adding it to the current [ComponentContainer]. **/
public suspend fun <M : ModalForm> ComponentContainer.publicSelectMenu(
    modal: (() -> M)?,
    row: Int? = null,
    builder: suspend PublicSelectMenu<M>.() -> Unit
): PublicSelectMenu<M> {
    val component = PublicSelectMenu(timeoutTask, modal)

    builder(component)
    add(component, row)

    return component
}

/** Convenience function for applying the components in a [ComponentContainer] to a message you're creating. **/
public suspend fun MessageCreateBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        applyToMessage()
    }
}

/** Convenience function for applying the components in a [ComponentContainer] to a message you're editing. **/
public suspend fun MessageModifyBuilder.applyComponents(components: ComponentContainer) {
    with(components) {
        applyToMessage()
    }
}

/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * creating. Supply a [timeout] and the components you add will be removed from the registry after the given period
 * of inactivity.
 */
public suspend fun MessageCreateBuilder.components(
    timeout: Duration? = null,
    builder: suspend ComponentContainer.() -> Unit
): ComponentContainer {
    val container = ComponentContainer(timeout, true, builder)

    applyComponents(container)

    return container
}

/**
 * Convenience function for creating a [ComponentContainer] and components, and applying it to a message you're
 * editing. Supply a [timeout] and the components you add will be removed from the registry after the given period
 * of inactivity.
 */
public suspend fun MessageModifyBuilder.components(
    timeout: Duration? = null,
    builder: suspend ComponentContainer.() -> Unit
): ComponentContainer {
    val container = ComponentContainer(timeout, true, builder)

    applyComponents(container)

    return container
}
