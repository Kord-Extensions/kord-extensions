/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.usagelimits

import com.kotlindiscord.kord.extensions.commands.events.ApplicationCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.ChatCommandInvocationEvent
import com.kotlindiscord.kord.extensions.commands.events.CommandInvocationEvent
import com.kotlindiscord.kord.extensions.utils.getLocale
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.cache.data.UserData
import java.util.*

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
    /** Locale of this command's executor. **/
    public val locale: suspend () -> Locale
) {
    public constructor(
        event: ChatCommandInvocationEvent,
    ) : this(
        event.event.message.data.author,
        event.event.message.channel,
        event.event.message.data.guildId.value,
        event,
        { event.event.getLocale() },
    )

    public constructor(
        event: ApplicationCommandInvocationEvent<*, *>,
    ) : this(
        event.event.interaction.user.data,
        event.event.interaction.channel,
        event.event.interaction.invokedCommandGuildId,
        event,
        { event.event.getLocale() }
    )
}
