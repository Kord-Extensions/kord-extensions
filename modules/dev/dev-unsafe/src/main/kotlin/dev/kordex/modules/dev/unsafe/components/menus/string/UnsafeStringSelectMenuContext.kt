/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
@file:OptIn(UnsafeAPI::class)

package dev.kordex.modules.dev.unsafe.components.menus.string

import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.menus.SelectMenuContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.components.menus.UnsafeSelectMenuInteractionContext

public class UnsafeStringSelectMenuContext<M : UnsafeModalForm> (
	component: UnsafeStringSelectMenu<M>,
	event: SelectMenuInteractionCreateEvent,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache), UnsafeSelectMenuInteractionContext {
	/** Menu options selected by the user before de-focusing the menu. **/
	public val selected: List<String> by lazy { event.interaction.values }
}
