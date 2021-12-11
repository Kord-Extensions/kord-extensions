/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.buttons

import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent

/** Class representing the execution context for a public-only button. **/
public class PublicInteractionButtonContext(
    component: PublicInteractionButton,
    event: ButtonInteractionCreateEvent,
    override val interactionResponse: PublicInteractionResponseBehavior
) : InteractionButtonContext(component, event), PublicInteractionContext
