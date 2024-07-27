/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application.slash

import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.PublicInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Public-only slash command context. **/
public class PublicSlashCommandContext<A : Arguments, M : ModalForm>(
	override val event: ChatInputCommandInteractionCreateEvent,
	override val command: SlashCommand<PublicSlashCommandContext<A, M>, A, M>,
	override val interactionResponse: PublicMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : SlashCommandContext<PublicSlashCommandContext<A, M>, A, M>(event, command, cache), PublicInteractionContext
