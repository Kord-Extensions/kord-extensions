/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Class representing the execution context for an ephemeral-only select (dropdown) menu. **/
public class EphemeralSelectMenuContext(
    override val component: Component,
    override val event: SelectMenuInteractionCreateEvent,
    override val interactionResponse: EphemeralMessageInteractionResponseBehavior
) : SelectMenuContext(component, event), EphemeralInteractionContext
