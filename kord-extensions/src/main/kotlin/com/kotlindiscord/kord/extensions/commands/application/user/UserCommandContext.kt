package com.kotlindiscord.kord.extensions.commands.application.user

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import dev.kord.core.event.interaction.UserCommandInteractionCreateEvent

/**
 *  User command context, containing everything you need for your user command's execution.
 *
 *  @param event Event that triggered this message command.
 *  @param command Message command instance.
 */
public class UserCommandContext(
    event: UserCommandInteractionCreateEvent,
    command: UserCommand
) : ApplicationCommandContext(event, command)
