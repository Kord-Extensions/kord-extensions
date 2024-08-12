/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
