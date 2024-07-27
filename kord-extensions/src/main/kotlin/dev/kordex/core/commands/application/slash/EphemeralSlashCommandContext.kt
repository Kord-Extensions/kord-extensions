/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.commands.application.slash

import dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kordex.core.commands.Arguments
import dev.kordex.core.components.forms.ModalForm
import dev.kordex.core.types.EphemeralInteractionContext
import dev.kordex.core.utils.MutableStringKeyedMap

/** Ephemeral-only slash command context. **/
public class EphemeralSlashCommandContext<A : Arguments, M : ModalForm>(
	override val event: ChatInputCommandInteractionCreateEvent,
	override val command: SlashCommand<EphemeralSlashCommandContext<A, M>, A, M>,
	override val interactionResponse: EphemeralMessageInteractionResponseBehavior,
	cache: MutableStringKeyedMap<Any>,
) : SlashCommandContext<EphemeralSlashCommandContext<A, M>, A, M>(event, command, cache), EphemeralInteractionContext
