/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.role

import com.kotlindiscord.kord.extensions.components.menus.OPTIONS_MAX
import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import dev.kord.rest.builder.component.ActionRowBuilder

/** Interface for role select menus. **/
public interface RoleSelectMenu {

    /** Apply the role select menu to an action row builder. **/
    public fun applyRoleSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
        if (selectMenu.maximumChoices == null) selectMenu.maximumChoices = OPTIONS_MAX

        builder.roleSelect(selectMenu.id) {
            this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
            this.placeholder = selectMenu.placeholder
            this.disabled = selectMenu.disabled
        }
    }
}
