/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.user

import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.message.create.InteractionResponseCreateBuilder
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.components.menus.PublicSelectMenu
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.scheduling.Task

public typealias InitialPublicSelectMenuResponseBuilder =
	(suspend InteractionResponseCreateBuilder.(SelectMenuInteractionCreateEvent) -> Unit)?

/** Class representing a public-only user select (dropdown) menu. **/
public open class PublicUserSelectMenu<M : ModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : PublicSelectMenu<PublicUserSelectMenuContext<M>, M>(timeoutTask), UserSelectMenu {
	override fun createContext(
		event: SelectMenuInteractionCreateEvent,
		interactionResponse: PublicMessageInteractionResponseBehavior,
		cache: MutableStringKeyedMap<Any>,
	): PublicUserSelectMenuContext<M> = PublicUserSelectMenuContext(
		this, event, interactionResponse, cache
	)

	override fun apply(builder: ActionRowBuilder): Unit = applyUserSelectMenu(this, builder)
}
