package com.kotlindiscord.kord.extensions.commands.cooldowns.base

import com.kotlindiscord.kord.extensions.commands.Command
import com.kotlindiscord.kord.extensions.commands.MessageCommand
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
import com.kotlindiscord.kord.extensions.commands.slash.SlashCommand

/**
 * A base class that holds information about a command and its cooldown. Used for importing/exporting cooldowns.
 */
public interface CommandCooldown<T : Command> {
    /** The command this class is for. **/
    public val command: T

    /** An immutable cooldown provider for the command class. **/
    public val cooldown: CooldownProvider
}

/**
 * A class that holds information about a message command and its cooldown.
 */
@JvmInline
@Suppress("ModifierOrder", "UseDataClass")
public value class MessageCommandCooldown(
    override val command: MessageCommand<out Arguments>
) : CommandCooldown<MessageCommand<out Arguments>> {
    override val cooldown: CooldownProvider get() = command.cooldown
}

/**
 * A class that holds information about a slash command and its cooldown.
 */
@JvmInline
@Suppress("ModifierOrder", "UseDataClass")
public value class SlashCommandCooldown(
    override val command: SlashCommand<out Arguments>
) : CommandCooldown<SlashCommand<out Arguments>> {
    override val cooldown: CooldownProvider get() = command.cooldown
}
