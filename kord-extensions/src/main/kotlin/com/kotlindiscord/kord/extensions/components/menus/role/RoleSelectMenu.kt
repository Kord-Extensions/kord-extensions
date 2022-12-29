/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.role

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.*
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.rest.builder.component.ActionRowBuilder

/** Abstract class representing a role select (dropdown) menu component. **/
public abstract class RoleSelectMenu<C : RoleSelectMenuContext, M : ModalForm>(timeoutTask: Task?) :
    SelectMenu<C, M>(timeoutTask) {

    public override fun apply(builder: ActionRowBuilder) {
        if (maximumChoices == null) maximumChoices = OPTIONS_MAX

        builder.roleSelect(id) {
            this.allowedValues = minimumChoices..maximumChoices!!
            this.placeholder = this@RoleSelectMenu.placeholder
            this.disabled = this@RoleSelectMenu.disabled
        }
    }
}
