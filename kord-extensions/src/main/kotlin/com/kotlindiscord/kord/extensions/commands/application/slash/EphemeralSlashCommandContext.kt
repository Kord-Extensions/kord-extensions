/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.types.EphemeralInteractionContext
import dev.kord.core.behavior.interaction.EphemeralInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/** Ephemeral-only slash command context. **/
public class EphemeralSlashCommandContext<A : Arguments>(
    override val event: ChatInputCommandInteractionCreateEvent,
    override val command: SlashCommand<EphemeralSlashCommandContext<A>, A>,
    override val interactionResponse: EphemeralInteractionResponseBehavior
) : SlashCommandContext<EphemeralSlashCommandContext<A>, A>(event, command), EphemeralInteractionContext
