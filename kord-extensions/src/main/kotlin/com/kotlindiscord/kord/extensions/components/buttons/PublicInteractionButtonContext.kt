/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Class representing the execution context for a public-only button. **/
public class PublicInteractionButtonContext<M : ModalForm>(
    component: PublicInteractionButton<M>,
    event: ButtonInteractionCreateEvent,
    override val interactionResponse: PublicMessageInteractionResponseBehavior,
    cache: MutableStringKeyedMap<Any>
) : InteractionButtonContext(component, event, cache), PublicInteractionContext
