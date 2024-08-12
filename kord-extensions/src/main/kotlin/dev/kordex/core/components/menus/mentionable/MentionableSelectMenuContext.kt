/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus.mentionable

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.RoleBehavior
import dev.kord.core.behavior.UserBehavior
import dev.kord.core.entity.KordEntity
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.components.menus.SelectMenu
import dev.kordex.core.components.menus.SelectMenuContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Abstract class representing the execution context of a user select (dropdown) menu component. **/
@OptIn(InternalAPI::class)
public abstract class MentionableSelectMenuContext(
	component: SelectMenu<*, *>,
	event: SelectMenuInteractionCreateEvent,
	cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache) {
	/** Roles selected by the user before de-focusing the menu. **/
	public val selectedRoles: List<RoleBehavior> by lazy {
		// Wrapping another context makes consistent behaviour easier.
		DummyRoleSelectMenuContext(component, event, cache).selected
	}

	/** Users selected by the user before de-focusing the menu. **/
	public val selectedUsers: List<UserBehavior> by lazy {
		// Wrapping another context makes consistent behaviour easier.
		DummyUserSelectMenuContext(component, event, cache).selected
	}

	/**
	 * Snowflakes representing menu options selected by the user before de-focusing the menu.
	 *
	 * Use the more-specific [selectedRoles] and [selectedUsers] properties to get the actual
	 * [KordEntity] behaviour objects.
	 *
	 * @see selectedRoles
	 * @see selectedUsers
	 */
	public val selected: List<Snowflake> by lazy {
		selectedUsers.map { it.id } +
			selectedRoles.map { it.id }
	}
}
