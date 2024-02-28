/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.string

import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import com.kotlindiscord.kord.extensions.components.menus.SelectMenuContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Abstract class representing the execution context of a string select (dropdown) menu component. **/
public abstract class StringSelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache) {
	/** Menu options that were selected by the user before de-focusing the menu. **/
	public val selected: List<String> by lazy { event.interaction.values }
}
