/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
