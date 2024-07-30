/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.role

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.RoleBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.menus.SelectMenu
import dev.kordex.core.components.menus.SelectMenuContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Abstract class representing the execution context of a role select (dropdown) menu component. **/
public abstract class RoleSelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache) {
	/** Menu options that were selected by the user before de-focusing the menu. **/
	@OptIn(KordUnsafe::class, KordExperimental::class)
	public val selected: List<RoleBehavior> by lazy {
		if (event.interaction.data.guildId.value == null) {
			error("A role select menu cannot be used outside guilds.")
		} else {
			event.interaction.resolvedObjects?.roles?.map { r -> r.value }
				?: event.interaction.values.map { r ->
					event.kord.unsafe.role(event.interaction.data.guildId.value!!, Snowflake(r))
				}
		}
	}
}
