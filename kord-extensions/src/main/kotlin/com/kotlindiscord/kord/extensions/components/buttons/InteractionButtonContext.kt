/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.components.ComponentContext
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Abstract class representing the execution context for a button component's action. **/
public abstract class InteractionButtonContext(
    component: Component,
    event: ButtonInteractionCreateEvent
) : ComponentContext<ButtonInteractionCreateEvent>(component, event)
