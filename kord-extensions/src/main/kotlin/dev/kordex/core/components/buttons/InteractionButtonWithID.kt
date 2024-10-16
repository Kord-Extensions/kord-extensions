/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.buttons

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kordex.core.components.ComponentWithID
import dev.kordex.core.components.types.HasPartialEmoji
import dev.kordex.core.i18n.types.Key

/** Abstract class representing a button component with an ID, but without a click action. **/
public abstract class InteractionButtonWithID : ComponentWithID(), HasPartialEmoji {
	/** Button label, for display on Discord. **/
	public var label: Key? = null
	public override var partialEmoji: DiscordPartialEmoji? = null

	override fun validate() {
		super.validate()

		if (label == null && partialEmoji == null) {
			error("Buttons must have either a label or emoji.")
		}
	}
}
