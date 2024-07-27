/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.buttons

import dev.kord.common.entity.ButtonStyle
import dev.kord.rest.builder.component.ActionRowBuilder

/** Class representing a disabled button component, which has no action. **/
public open class DisabledInteractionButton : InteractionButtonWithID() {
	/** Button style - anything but Link is valid. **/
	public open var style: ButtonStyle = ButtonStyle.Primary

	override fun apply(builder: ActionRowBuilder) {
		builder.interactionButton(style, id) {
			emoji = partialEmoji
			label = this@DisabledInteractionButton.label

			disabled = true
		}
	}

	override fun validate() {
		super.validate()

		if (style == ButtonStyle.Link) {
			error("The Link button style is reserved for link buttons.")
		}
	}
}
