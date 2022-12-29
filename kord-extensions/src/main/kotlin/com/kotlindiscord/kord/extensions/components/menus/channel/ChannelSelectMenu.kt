/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.channel

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.components.menus.*
import com.kotlindiscord.kord.extensions.utils.scheduling.Task
import dev.kord.common.entity.ChannelType
import dev.kord.rest.builder.component.ActionRowBuilder

/** Abstract class representing a channel select (dropdown) menu component. **/
public abstract class ChannelSelectMenu<C : ChannelSelectMenuContext, M : ModalForm>(timeoutTask: Task?) :
    SelectMenu<C, M>(timeoutTask) {

    /** The channel types allowed to be selected in the dropdown. **/
    public var channelTypes: MutableList<ChannelType> = mutableListOf()

    /** Add an allowed channel type to the selector. **/
    public fun channelType(vararg type: ChannelType) {
        channelTypes.addAll(type)
    }

    public override fun apply(builder: ActionRowBuilder) {
        if (maximumChoices == null) maximumChoices = OPTIONS_MAX

        builder.channelSelect(id) {
            this.channelTypes = if (this@ChannelSelectMenu.channelTypes.isEmpty()) {
                null
            } else {
                this@ChannelSelectMenu.channelTypes
            }
            this.allowedValues = minimumChoices..maximumChoices!!
            this.placeholder = this@ChannelSelectMenu.placeholder
            this.disabled = this@ChannelSelectMenu.disabled
        }
    }
}
