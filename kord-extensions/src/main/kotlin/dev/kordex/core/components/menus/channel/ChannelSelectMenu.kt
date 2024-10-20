/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus.channel

import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.components.menus.OPTIONS_MAX
import dev.kordex.core.components.menus.SelectMenu

/** Interface for channel select menus. **/
public interface ChannelSelectMenu {
	/** The types allowed in the select menu. **/
	public var channelTypes: MutableList<ChannelType>

	/** Default channels to preselect. **/
	public var defaultChannels: MutableList<Snowflake>

	/** Add an allowed channel type to the selector. **/
	public fun channelType(vararg type: ChannelType) {
		channelTypes.addAll(type)
	}

	/** Add a default pre-selected channel to the selector. **/
	public fun defaultChannel(id: Snowflake) {
		defaultChannels.add(id)
	}

	/** Add a default pre-selected channel to the selector. **/
	public fun defaultChannel(channel: ChannelBehavior) {
		defaultChannel(channel.id)
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

			this@ChannelSelectMenu.defaultChannels.forEach(this.defaultChannels::add)

			this.allowedValues = selectMenu.minimumChoices..selectMenu.maximumChoices!!
			this.disabled = selectMenu.disabled
			this.placeholder = selectMenu.placeholder?.translate()
		}
	}
}
