/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
			this.placeholder = selectMenu.placeholder
			this.disabled = selectMenu.disabled
		}
	}
}
