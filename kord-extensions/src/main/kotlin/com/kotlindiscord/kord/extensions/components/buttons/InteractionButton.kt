/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.components.types.HasPartialEmoji
import dev.kord.common.entity.DiscordPartialEmoji

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
