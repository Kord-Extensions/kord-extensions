/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus.user

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.menus.SelectMenu
import dev.kordex.core.components.menus.SelectMenuContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Abstract class representing the execution context of a user select (dropdown) menu component. **/
public abstract class UserSelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache) {
	/** Menu options that were selected by the user before de-focusing the menu. **/
	@OptIn(KordUnsafe::class, KordExperimental::class)
	public val selected: List<UserBehavior> by lazy {
		val users: MutableList<UserBehavior> = mutableListOf()

		event.interaction.resolvedObjects?.users?.forEach { users.add(it.value) }
		event.interaction.resolvedObjects?.members?.forEach { users.add(it.value) }

		if (users.isEmpty()) {
			event.interaction.values.forEach { users.add(event.kord.unsafe.user(Snowflake(it))) }
		}

		users.groupBy { it.id }.values.map { it.last() }
	}
}
