package com.kotlindiscord.kord.extensions.commands.events

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import dev.kord.core.event.interaction.ApplicationCommandInteractionCreateEvent

// region Invocation events

/** Basic event emitted when am application command is invoked. **/
public interface ApplicationCommandInvocationEvent<
    C : ApplicationCommand<*>,
    E : ApplicationCommandInteractionCreateEvent
> : CommandInvocationEvent<C, E>
