/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus

import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.ComponentContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Abstract class representing the execution context of a select (dropdown) menu component. **/
@Suppress("UnnecessaryAbstractClass")
public abstract class SelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : ComponentContext<SelectMenuInteractionCreateEvent>(component, event, cache)
