package com.kotlindiscord.kord.extensions.commands.application.slash

import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommandContext
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent

/**
 * Slash command context, containing everything you need for your slash command's execution.
 *
 * @param event Event that triggered this slash command invocation.
 */
public open class SlashCommandContext<C : SlashCommandContext<C, A>, A : Arguments>(
    public open val event: ChatInputCommandInteractionCreateEvent,
    public override val command: SlashCommand<C, A>
) : ApplicationCommandContext(event, command) {
    /** Object representing this slash command's arguments, if any. **/
    public open lateinit var arguments: A

    /** @suppress Internal function for copying args object in later. **/
    public fun populateArgs(args: A) {
        arguments = args
    }
}
