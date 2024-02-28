/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.ComponentContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Abstract class representing the execution context of a select (dropdown) menu component. **/
@Suppress("UnnecessaryAbstractClass")
public abstract class SelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : ComponentContext<SelectMenuInteractionCreateEvent>(component, event, cache)
