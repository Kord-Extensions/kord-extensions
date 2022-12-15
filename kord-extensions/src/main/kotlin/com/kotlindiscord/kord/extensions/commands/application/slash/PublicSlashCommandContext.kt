/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.components.forms.ModalForm
import com.kotlindiscord.kord.extensions.types.PublicInteractionContext
import com.kotlindiscord.kord.extensions.utils.MutableStringKeyedMap
import dev.kord.core.behavior.interaction.response.PublicMessageInteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/** Public-only slash command context. **/
public class PublicSlashCommandContext<A : Arguments, M : ModalForm>(
    override val event: ChatInputCommandInteractionCreateEvent,
    override val command: SlashCommand<PublicSlashCommandContext<A, M>, A, M>,
    override val interactionResponse: PublicMessageInteractionResponseBehavior,
    cache: MutableStringKeyedMap<Any>
) : SlashCommandContext<PublicSlashCommandContext<A, M>, A, M>(event, command, cache), PublicInteractionContext
