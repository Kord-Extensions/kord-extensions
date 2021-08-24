package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import dev.kord.core.event.interaction.ApplicationInteractionCreateEvent

/** Slash command context, containing everything you need for your slash command's execution. **/
public class SlashCommandContext(
    genericEvent: ApplicationInteractionCreateEvent,
    genericCommand: ApplicationCommand<*>
) : ApplicationCommandContext(genericEvent, genericCommand)
