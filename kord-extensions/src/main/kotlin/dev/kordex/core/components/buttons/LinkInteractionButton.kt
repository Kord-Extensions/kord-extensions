/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.buttons

import dev.kord.rest.builder.component.ActionRowBuilder

/** Class representing a linked button component, which opens a URL when clicked. **/
public open class LinkInteractionButton : InteractionButton() {
	/** URL to send the user to when clicked. **/
	public open lateinit var url: String

	override fun validate() {
		super.validate()

		if (!this::url.isInitialized) {
			error("Link buttons must have a URL.")
		}
	}

	override fun apply(builder: ActionRowBuilder) {
		builder.linkButton(url) {
			emoji = partialEmoji
			label = this@LinkInteractionButton.label?.translate()
		}
	}
}
