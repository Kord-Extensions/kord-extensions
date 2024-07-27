/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.channel

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.EphemeralInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Class representing the execution context for an ephemeral-only channel select (dropdown) menu. **/
public class EphemeralChannelSelectMenuContext<M : ModalForm>(
	override val component: EphemeralChannelSelectMenu<M>,
	override val event: SelectMenuInteractionCreateEvent,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : ChannelSelectMenuContext(component, event, cache), EphemeralInteractionContext
