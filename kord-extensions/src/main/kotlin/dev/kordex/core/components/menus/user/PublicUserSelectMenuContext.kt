/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.components.menus.user

import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.PublicInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Class representing the execution context for a public-only user select (dropdown) menu. **/
public class PublicUserSelectMenuContext<M : ModalForm>(
	override val component: PublicUserSelectMenu<M>,
	override val event: SelectMenuInteractionCreateEvent,
	override val interactionResponse: PublicMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : UserSelectMenuContext(component, event, cache), PublicInteractionContext
