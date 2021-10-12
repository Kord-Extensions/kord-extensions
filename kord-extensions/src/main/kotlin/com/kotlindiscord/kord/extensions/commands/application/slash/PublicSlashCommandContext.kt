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
