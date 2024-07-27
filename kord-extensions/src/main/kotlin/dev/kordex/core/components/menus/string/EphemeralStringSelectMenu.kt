/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.string

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.SelectOptionBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.menus.EphemeralSelectMenu
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.scheduling.Task

public typealias InitialEphemeralSelectMenuResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

/** Class representing an ephemeral-only string select (dropdown) menu. **/
public open class EphemeralStringSelectMenu<M : ModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : EphemeralSelectMenu<EphemeralStringSelectMenuContext<M>, M>(timeoutTask), StringSelectMenu {
	override val options: MutableList<SelectOptionBuilder> = mutableListOf()

	override fun createContext(
		event: SelectMenuInteractionCreateEvent,
		interactionResponse: EphemeralMessageInteractionResponseBehavior,
		cache: MutableStringKeyedMap<Any>,
	): EphemeralStringSelectMenuContext<M> = EphemeralStringSelectMenuContext(
		this, event, interactionResponse, cache
	)

	override fun apply(builder: ActionRowBuilder): Unit = applyStringSelectMenu(this, builder)
}
