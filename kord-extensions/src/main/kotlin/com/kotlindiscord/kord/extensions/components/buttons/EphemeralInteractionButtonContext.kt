/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Class representing the execution context for an ephemeral-only button. **/
public class EphemeralInteractionButtonContext(
    override val component: EphemeralInteractionButton,
    override val event: ButtonInteractionCreateEvent,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : InteractionButtonContext(component, event), EphemeralInteractionContext
