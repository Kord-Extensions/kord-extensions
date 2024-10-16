/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components.menus.string

import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kordex.core.i18n.types.Key

public class StringSelectOption(
	public var label: Key,
	public var value: String
) {
	public var description: Key? = null
	public var emoji: DiscordPartialEmoji? = null
	public var default: Boolean = false

	public fun build(): SelectOptionBuilder = SelectOptionBuilder(
		label.translate(),
		value
	).apply {
		this.default = this@StringSelectOption.default

		if (this@StringSelectOption.description != null) {
			this.description = this@StringSelectOption.description!!.translate()
		}

		if (this@StringSelectOption.description != null) {
			this.emoji = this@StringSelectOption.emoji!!
		}
	}
}
