/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.user

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.*
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.rest.builder.component.ActionRowBuilder

/** Abstract class representing a user select (dropdown) menu component. **/
public abstract class UserSelectMenu<C : UserSelectMenuContext, M : ModalForm>(timeoutTask: Task?) :
    SelectMenu<C, M>(timeoutTask) {

    public override fun apply(builder: ActionRowBuilder) {
        if (maximumChoices == null) maximumChoices = OPTIONS_MAX

        builder.userSelect(id) {
            this.allowedValues = minimumChoices..maximumChoices!!
            this.placeholder = this@UserSelectMenu.placeholder
            this.disabled = this@UserSelectMenu.disabled
        }
    }
}
