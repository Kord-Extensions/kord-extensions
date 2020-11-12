package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.DmChannel
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.MultiConverter
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MessageListConverter(
    required: Boolean = true,
    private var requireGuild: Boolean = false,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : MultiConverter<Message>(required) {
    override val signatureTypeString = "messages"

    override suspend fun parse(args: List<String>, context: CommandContext, bot: ExtensibleBot): Int {
        val message = mutableListOf<Message>()

        for (arg in args) {
            message.add(
                findMessage(arg, context, bot) ?: break
            )
        }

        parsed = message.toList()

        return parsed.size
    }

    private suspend fun findMessage(arg: String, context: CommandContext, bot: ExtensibleBot): Message? {
        val requiredGid = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId

        return if (arg.startsWith("https://")) { // It's a message URL
            val split = arg.substring(8).split("/").takeLast(3)

            if (split.size < 3) {
                logger.debug { "Invalid message link: $arg" }

                return null
            }

            val gid = try {
                Snowflake(split[2])
            } catch (e: NumberFormatException) {
                logger.debug { "Invalid server ID: ${split[2]}" }

                return null
            }

            if (requireGuild && requiredGid != gid) {
                logger.debug { "Matching guild ($requiredGid) required, but guild ($gid) doesn't match." }

                return null
            }

            val cid = try {
                Snowflake(split[3])
            } catch (e: NumberFormatException) {
                logger.debug { "Invalid channel ID: ${split[3]}" }

                return null
            }

            val channel = bot.kord.getGuild(gid)?.getChannel(cid)

            if (channel == null) {
                logger.debug { "Unable to find channel ($cid) for guild ($gid)." }

                return null
            }

            if (channel !is GuildMessageChannel) {
                logger.debug { "Specified channel ($cid) is not a guild message channel." }

                return null
            }

            val mid = try {
                Snowflake(split[4])
            } catch (e: NumberFormatException) {
                logger.debug { "Invalid message ID: ${split[4]}" }

                return null
            }

            channel.getMessage(mid)
        } else { // Try a message ID
            val channel = context.message.channel.asChannelOrNull()

            if (channel !is GuildMessageChannel && channel !is DmChannel) {
                logger.debug { "Current channel is not a guild message channel or DM channel." }

                return null
            }

            try {
                channel.getMessage(Snowflake(arg))
            } catch (e: NumberFormatException) {
                logger.debug { "Invalid channel ID: $arg" }

                return null
            }
        }
    }
}
