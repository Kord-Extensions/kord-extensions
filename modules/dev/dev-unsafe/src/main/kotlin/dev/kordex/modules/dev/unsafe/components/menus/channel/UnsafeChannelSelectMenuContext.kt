/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
@file:OptIn(UnsafeAPI::class)

package dev.kordex.modules.dev.unsafe.components.menus.channel

import dev.kord.common.annotation.KordExperimental
import dev.kord.common.annotation.KordUnsafe
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.ChannelBehavior
import dev.kord.core.behavior.interaction.response.MessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.menus.SelectMenuContext
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.dev.unsafe.annotations.UnsafeAPI
import dev.kordex.modules.dev.unsafe.components.forms.UnsafeModalForm
import dev.kordex.modules.dev.unsafe.components.menus.UnsafeSelectMenuInteractionContext

public class UnsafeChannelSelectMenuContext<M : UnsafeModalForm> (
	component: UnsafeChannelSelectMenu<M>,
	event: SelectMenuInteractionCreateEvent,
	override var interactionResponse: MessageInteractionResponseBehavior?,
	cache: MutableStringKeyedMap<Any>,
) : SelectMenuContext(component, event, cache), UnsafeSelectMenuInteractionContext {
	/** Menu options selected by the user before de-focusing the menu. **/
	@OptIn(KordUnsafe::class, KordExperimental::class)
	public val selected: List<ChannelBehavior> by lazy {
		event.interaction.resolvedObjects?.channels?.map { it.value }
			?: event.interaction.values.map { event.kord.unsafe.channel(Snowflake(it)) }
	}
}
