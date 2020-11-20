package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.entity.channel.GuildChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.channel
import com.kotlindiscord.kord.extensions.commands.converters.channelList
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList

/**
 * Argument converter for Discord [Channel] arguments.
 *
 * This converter supports specifying channels by supplying:
 *
 * * A channel mention
 * * A channel ID, with or without a `#` prefix
 * * A channel name, with or without a `#` prefix (the required guild will be searched for the first matching channel)
 *
 * @param requireSameGuild Whether to require that the channel passed is on the same guild as the message.
 * @param requiredGuild Lambda returning a specific guild to require the channel to be in, if needed.
 *
 * @see channel
 * @see channelList
 */
public class ChannelConverter(
    private val requireSameGuild: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : SingleConverter<Channel>() {
    override val signatureTypeString: String = "channel"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val channel = findChannel(arg, context, bot)
            ?: throw ParseException("Unable to find channel: $arg")

        parsed = channel
        return true
    }

    private suspend fun findChannel(arg: String, context: CommandContext, bot: ExtensibleBot): Channel? {
        val channel: Channel? = if (arg.startsWith("<#") && arg.endsWith(">")) { // Channel mention
            val id = arg.substring(2, arg.length - 1)

            try {
                bot.kord.getChannel(Snowflake(id.toLong()))
            } catch (e: NumberFormatException) {
                throw ParseException("Value '$id' is not a valid channel ID.")
            }
        } else {
            val string = if (arg.startsWith("#")) arg.substring(1) else arg

            var foundChannel: Channel? = null

            try {
                foundChannel = bot.kord.getChannel(Snowflake(string.toLong()))
            } catch (e: NumberFormatException) { // It's not a numeric ID, so let's try a channel name
                for (channelObject in bot.kord.guilds.flatMapConcat { it.channels }.toList()) {
                    if (channelObject.name.equals(string, false)) {
                        foundChannel = channelObject
                    }
                }
            }

            foundChannel
        }

        if (channel is GuildChannel && (requireSameGuild || requiredGuild != null)) {
            val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId

            if (requireSameGuild && channel.guildId != guildId) {
                return null  // Channel isn't in the right guild
            }
        }

        return channel
    }
}
