/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.buttons

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kordex.core.components.Component
import dev.kordex.core.components.types.HasPartialEmoji

/** Abstract class representing a button component. **/
public abstract class InteractionButton : Component(), HasPartialEmoji {
	/** Button label, for display on Discord. **/
	public var label: String? = null

	public override var partialEmoji: DiscordPartialEmoji? = null

	override fun validate() {
		if (label == null && partialEmoji == null) {
			error("Buttons must have either a label or emoji.")
		}
	}
}
