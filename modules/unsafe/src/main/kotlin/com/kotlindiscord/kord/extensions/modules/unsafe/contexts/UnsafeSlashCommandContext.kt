package com.kotlindiscord.kord.extensions.modules.unsafe.contexts

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.slash.SlashCommandContext
import com.kotlindiscord.kord.extensions.modules.unsafe.annotations.UnsafeAPI
import com.kotlindiscord.kord.extensions.modules.unsafe.commands.UnsafeSlashCommand
import com.kotlindiscord.kord.extensions.modules.unsafe.types.UnsafeInteractionContext
import dev.kord.core.behavior.interaction.InteractionResponseBehavior
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/** Command context for an unsafe slash command. **/
@UnsafeAPI
public class UnsafeSlashCommandContext<A : Arguments>(
    override val event: ChatInputCommandInteractionCreateEvent,
    override val command: UnsafeSlashCommand<A>,
    override var interactionResponse: InteractionResponseBehavior?
) : SlashCommandContext<UnsafeSlashCommandContext<A>, A>(event, command), UnsafeInteractionContext
