/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.mentionable

import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.components.menus.SelectMenu
import dev.kordex.core.components.menus.role.RoleSelectMenuContext
import dev.kordex.core.components.menus.user.UserSelectMenuContext
import dev.kordex.core.utils.MutableStringKeyedMap

@InternalAPI
public class DummyRoleSelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : RoleSelectMenuContext(component, event, cache)

@InternalAPI
public class DummyUserSelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : UserSelectMenuContext(component, event, cache)
