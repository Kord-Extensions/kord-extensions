/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.role

import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import com.kotlindiscord.kord.extensions.components.menus.SelectMenuContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.RoleBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

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
			event.interaction.values.map {
				event.kord.unsafe.role(event.interaction.data.guildId.value!!, Snowflake(it))
			}
		}
	}
}
