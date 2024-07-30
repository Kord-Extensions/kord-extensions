/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.role

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.RoleBehavior
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.components.menus.OPTIONS_MAX
import dev.kordex.core.components.menus.SelectMenu

/** Interface for role select menus. **/
public interface RoleSelectMenu {
	/** Default roles to preselect. **/
	public var defaultRoles: MutableList<Snowflake>

	/** Add a default pre-selected role to the selector. **/
	public fun defaultRole(id: Snowflake) {
		defaultRoles.add(id)
	}

	/** Add a default pre-selected role to the selector. **/
	public fun defaultRole(role: RoleBehavior) {
		defaultRole(role.id)
	}

	/** Apply the role select menu to an action row builder. **/
	public fun applyRoleSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
		if (selectMenu.maximumChoices == null) selectMenu.maximumChoices = OPTIONS_MAX

		builder.roleSelect(selectMenu.id) {
			this@RoleSelectMenu.defaultRoles.forEach(this.defaultRoles::add)

			this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
			this.disabled = selectMenu.disabled
			this.placeholder = selectMenu.placeholder
		}
	}
}
