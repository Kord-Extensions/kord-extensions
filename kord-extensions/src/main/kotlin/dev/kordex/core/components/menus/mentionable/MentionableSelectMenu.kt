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
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.components.menus.OPTIONS_MAX
import dev.kordex.core.components.menus.SelectMenu

/** Interface for user select menus. **/
public interface MentionableSelectMenu {
	/** Default roles to preselect. **/
	public var defaultRoles: MutableList<Snowflake>

	/** Default users to preselect. **/
	public var defaultUsers: MutableList<Snowflake>

	/** Add a default pre-selected role to the selector. **/
	public fun defaultRole(id: Snowflake) {
		defaultRoles.add(id)
	}

	/** Add a default pre-selected role to the selector. **/
	public fun defaultRole(role: RoleBehavior) {
		defaultRole(role.id)
	}

	/** Add a default pre-selected channel to the selector. **/
	public fun defaultUser(id: Snowflake) {
		defaultUsers.add(id)
	}

	/** Add a default pre-selected channel to the selector. **/
	public fun defaultUser(user: UserBehavior) {
		defaultUser(user.id)
	}

	/** Apply the user select menu to an action row builder. **/
	public fun applyMentionableSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
		if (selectMenu.maximumChoices == null) selectMenu.maximumChoices = OPTIONS_MAX

		builder.mentionableSelect(selectMenu.id) {
			this@MentionableSelectMenu.defaultRoles.forEach(this.defaultRoles::add)
			this@MentionableSelectMenu.defaultUsers.forEach(this.defaultUsers::add)

			this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
			this.placeholder = selectMenu.placeholder?.translate()
			this.disabled = selectMenu.disabled
		}
	}
}
