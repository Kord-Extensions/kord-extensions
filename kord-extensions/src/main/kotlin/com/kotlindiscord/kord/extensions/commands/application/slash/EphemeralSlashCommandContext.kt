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
