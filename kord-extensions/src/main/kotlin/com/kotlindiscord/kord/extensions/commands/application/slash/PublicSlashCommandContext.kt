/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import dev.kord.core.behavior.interaction.PublicInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/** Public-only slash command context. **/
public class PublicSlashCommandContext<A : Arguments>(
    override val event: ChatInputCommandInteractionCreateEvent,
    override val command: SlashCommand<PublicSlashCommandContext<A>, A>,
    override val interactionResponse: PublicInteractionResponseBehavior
) : SlashCommandContext<PublicSlashCommandContext<A>, A>(event, command), PublicInteractionContext
