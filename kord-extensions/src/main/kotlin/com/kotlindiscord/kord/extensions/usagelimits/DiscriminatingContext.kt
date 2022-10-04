package com.kotlindiscord.kord.extensions.usagelimits

import com.kotlindiscord.kord.extensions.commands.events.ApplicationCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.CommandInvocationEvent
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.cache.data.UserData

/** Data holder for information about command invocation. **/
public data class DiscriminatingContext(
    /** Command invoker's [UserData]. **/
    public val user: UserData,
    /** [MessageChannelBehavior] of the messageChannel in which the command was invoked. **/
    public val channel: MessageChannelBehavior,
    /** guildId of the Guild in which the command was invoked, can be null if the command was invoked
     * in DMs. **/
    public val guildId: Snowflake?,
    /** Command invoker's [UserData]. **/
    public val event: CommandInvocationEvent<*, *>,
) {
    public constructor(
        event: ChatCommandInvocationEvent,
    ) : this(
        event.event.message.data.author,
        event.event.message.channel,
        event.event.message.data.guildId.value,
        event
    )

    public constructor(
        event: ApplicationCommandInvocationEvent<*, *>,
    ) : this(
        event.event.interaction.user.data,
        event.event.interaction.channel,
        event.event.interaction.invokedCommandGuildId,
        event
    )
}
