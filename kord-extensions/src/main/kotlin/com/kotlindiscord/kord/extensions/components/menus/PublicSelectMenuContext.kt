/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.components.menus

import com.kotlindiscord.kord.extensions.components.Component
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent

/** Class representing the execution context for a public-only select (dropdown) menu. **/
public class PublicSelectMenuContext(
    override val component: Component,
    override val event: SelectMenuInteractionCreateEvent,
    override val interactionResponse: PublicInteractionResponseBehavior
) : SelectMenuContext(component, event), PublicInteractionContext
