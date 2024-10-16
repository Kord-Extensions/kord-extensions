/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
			label = this@DisabledInteractionButton.label?.translate()

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
