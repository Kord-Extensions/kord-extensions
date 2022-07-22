/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.components.ComponentContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Abstract class representing the execution context for a button component's action. **/
@Suppress("UnnecessaryAbstractClass")  // Your face is an unnecessary abstract class
public abstract class InteractionButtonContext(
    component: Component,
    event: ButtonInteractionCreateEvent,
    cache: MutableStringKeyedMap<Any>
) : ComponentContext<ButtonInteractionCreateEvent>(component, event, cache)
