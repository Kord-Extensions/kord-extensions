/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Class representing the execution context for a public-only select (dropdown) menu. **/
public class PublicSelectMenuContext<M : ModalForm>(
    override val component: PublicSelectMenu<M>,
    override val event: SelectMenuInteractionCreateEvent,
    override val interactionResponse: PublicMessageInteractionResponseBehavior,
    cache: MutableStringKeyedMap<Any>
) : SelectMenuContext(component, event, cache), PublicInteractionContext
