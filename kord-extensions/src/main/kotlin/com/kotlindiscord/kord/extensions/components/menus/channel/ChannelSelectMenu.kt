/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.channel

import com.kotlindiscord.kord.extensions.components.menus.OPTIONS_MAX
import com.kotlindiscord.kord.extensions.components.menus.SelectMenu
import dev.kord.common.entity.ChannelType
import dev.kord.rest.builder.component.ActionRowBuilder

/** Interface for channel select menus. **/
public interface ChannelSelectMenu {
	/** The types allowed in the select menu. **/
	public var channelTypes: MutableList<ChannelType>

	/** Add an allowed channel type to the selector. **/
	public fun channelType(vararg type: ChannelType) {
		channelTypes.addAll(type)
	}

	/** Apply the channel select menu to an action row builder. **/
	public fun applyChannelSelectMenu(selectMenu: SelectMenu<*, *>, builder: ActionRowBuilder) {
		if (selectMenu.maximumChoices == null) selectMenu.maximumChoices = OPTIONS_MAX

		builder.channelSelect(selectMenu.id) {
			this.channelTypes = if (this@ChannelSelectMenu.channelTypes.isEmpty()) {
				null
			} else {
				this@ChannelSelectMenu.channelTypes
			}
			this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
			this.placeholder = selectMenu.placeholder
			this.disabled = selectMenu.disabled
		}
	}
}
