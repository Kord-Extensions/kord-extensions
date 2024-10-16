/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus.user

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.UserBehavior
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.components.menus.OPTIONS_MAX
import dev.kordex.core.components.menus.SelectMenu

/** Interface for user select menus. **/
public interface UserSelectMenu {
	/** Default users to preselect. **/
	public var defaultUsers: MutableList<Snowflake>

	/** Add a default pre-selected channel to the selector. **/
	public fun defaultUser(id: Snowflake) {
		defaultUsers.add(id)
	}

	/** Add a default pre-selected channel to the selector. **/
	public fun defaultUser(user: UserBehavior) {
		defaultUser(user.id)
	}

	/** Apply the user select menu to an action row builder. **/
	public fun applyUserSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
		if (selectMenu.maximumChoices == null) selectMenu.maximumChoices = OPTIONS_MAX

		builder.userSelect(selectMenu.id) {
			this@UserSelectMenu.defaultUsers.forEach(this.defaultUsers::add)

			this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
			this.disabled = selectMenu.disabled
			this.placeholder = selectMenu.placeholder?.translate()
		}
	}
}
