/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.buttons

import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kordex.core.components.Component
import dev.kordex.core.components.ComponentContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Abstract class representing the execution context for a button component's action. **/
@Suppress("UnnecessaryAbstractClass")  // Your face is an unnecessary abstract class
public abstract class InteractionButtonContext(
	component: Component,
	event: ButtonInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : ComponentContext<ButtonInteractionCreateEvent>(component, event, cache)
