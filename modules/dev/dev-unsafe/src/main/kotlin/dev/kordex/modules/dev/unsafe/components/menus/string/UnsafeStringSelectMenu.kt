/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package dev.kordex.modules.dev.unsafe.components.menus.string

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kordex.core.components.menus.string.StringSelectMenu
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.components.menus.UnsafeSelectMenu

@UnsafeAPI
public class UnsafeStringSelectMenu<M : UnsafeModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : UnsafeSelectMenu<UnsafeStringSelectMenuContext<M>, M>(timeoutTask), StringSelectMenu {
	override val options: MutableList<SelectOptionBuilder> = mutableListOf()

	override fun createContext(
		event: SelectMenuInteractionCreateEvent,
		interactionResponse: MessageInteractionResponseBehavior?,
		cache: MutableStringKeyedMap<Any>,
	): UnsafeStringSelectMenuContext<M> = UnsafeStringSelectMenuContext(
		this, event, interactionResponse, cache
	)

	override fun apply(builder: ActionRowBuilder): Unit = applyStringSelectMenu(this, builder)
}
