/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus.channel

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Class representing the execution context for an ephemeral-only channel select (dropdown) menu. **/
public class EphemeralChannelSelectMenuContext<M : ModalForm>(
    override val component: EphemeralChannelSelectMenu<M>,
    override val event: SelectMenuInteractionCreateEvent,
    override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
    cache: MutableStringKeyedMap<Any>
) : ChannelSelectMenuContext(component, event, cache), EphemeralInteractionContext
