/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package dev.kordex.modules.dev.unsafe.components.menus.mentionable

import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kordex.core.components.menus.mentionable.MentionableSelectMenu
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.components.menus.UnsafeSelectMenu

@UnsafeAPI
public class UnsafeMentionableSelectMenu<M : UnsafeModalForm>(
	timeoutTask: Task?,
	public override val modal: (() -> M)? = null,
) : UnsafeSelectMenu<UnsafeMentionableSelectMenuContext<M>, M>(timeoutTask), MentionableSelectMenu {
	override var defaultRoles: MutableList<Snowflake> = mutableListOf()
	override var defaultUsers: MutableList<Snowflake> = mutableListOf()

	override fun createContext(
		event: SelectMenuInteractionCreateEvent,
		interactionResponse: MessageInteractionResponseBehavior?,
		cache: MutableStringKeyedMap<Any>,
	): UnsafeMentionableSelectMenuContext<M> = UnsafeMentionableSelectMenuContext(
		this, event, interactionResponse, cache
	)

	override fun apply(builder: ActionRowBuilder): Unit = applyMentionableSelectMenu(this, builder)
}
