package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.channel.Channel
import com.gitlab.kordlib.core.entity.channel.GuildChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class ChannelListConverter(
    required: Boolean = true,
    private val requireSameGuild: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : MultiConverter<Channel>(required) {
    override val signatureTypeString = "channel"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val channels = mutableListOf<Channel>()

        for (arg in args) {
            channels.add(
                findChannel(arg, context, bot) ?: break
            )
        }

        parsed = channels.toList()

        return parsed.size
    }

    private suspend fun findChannel(arg: String, context: CommandContext, bot: ExtensibleBot): Channel? {
        val channel: Channel = if (arg.startsWith("<#") && arg.endsWith(">")) { // Channel mention
            val id = arg.substring(2, arg.length - 2)

            try {
                bot.kord.getChannel(Snowflake(id.toLong()))
            } catch (e: NumberFormatException) {
                logger.debug { "Value '$id' is not a valid channel ID." }

                null
            }
        } else {
            val string = if (arg.startsWith("#")) arg.substring(1) else arg

            var foundChannel: Channel? = null

            try {
                bot.kord.getChannel(Snowflake(string.toLong()))
            } catch (e: NumberFormatException) { // It's not a numeric ID, so let's try a channel name

                for (channelObject in bot.kord.guilds.flatMapConcat { it.channels }.toList()) {
                    if (channelObject.name.equals(string, false)) {
                        foundChannel = channelObject
                    }
                }
            }

            foundChannel
        } ?: return null

        if (channel is GuildChannel && (requireSameGuild || requiredGuild != null)) {
            val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId

            if (requireSameGuild && channel.guildId != guildId) {
                logger.debug { "Channel is not in the required guild." }

                return null  // Channel isn't in the right guild
            }
        }

        return channel
    }
}
