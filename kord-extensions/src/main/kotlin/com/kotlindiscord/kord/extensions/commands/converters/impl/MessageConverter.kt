package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.DmChannel
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class MessageConverter(
    required: Boolean = true,
    private var requireGuild: Boolean = false,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : SingleConverter<Message>(required) {
    override val signatureTypeString = "message"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val message = findMessage(arg, context, bot)
            ?: throw ParseException("Unable to find message: $arg")

        parsed = message
        return true
    }

    private suspend fun findMessage(arg: String, context: CommandContext, bot: ExtensibleBot): Message? {
        val requiredGid = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId

        return if (arg.startsWith("https://")) { // It's a message URL
            val split = arg.substring(8).split("/").takeLast(3)

            if (split.size < 3) {
                throw ParseException("Invalid message url provided: <$arg>")
            }

            val gid = try {
                Snowflake(split[2])
            } catch (e: NumberFormatException) {
                throw ParseException("Value '${split[2]}' is not a valid guild ID.")
            }

            if (requireGuild && requiredGid != gid) {
                logger.debug { "Matching guild ($requiredGid) required, but guild ($gid) doesn't match." }

                throw ParseException("Unable to find message: $arg")
            }

            val cid = try {
                Snowflake(split[3])
            } catch (e: NumberFormatException) {
                throw ParseException("Value '${split[3]}' is not a valid channel ID.")
            }

            val channel = bot.kord.getGuild(gid)?.getChannel(cid)

            if (channel == null) {
                logger.debug { "Unable to find channel ($cid) for guild ($gid)." }

                throw ParseException("Unable to find message: $arg")
            }

            if (channel !is GuildMessageChannel) {
                logger.debug { "Specified channel ($cid) is not a guild message channel." }

                throw ParseException("Unable to find message: $arg")
            }

            val mid = try {
                Snowflake(split[4])
            } catch (e: NumberFormatException) {
                throw ParseException("Value '${split[4]}' is not a valid message ID.")
            }

            channel.getMessage(mid)
        } else { // Try a message ID
            val channel = context.message.channel.asChannelOrNull()

            if (channel !is GuildMessageChannel && channel !is DmChannel) {
                logger.debug { "Current channel is not a guild message channel or DM channel." }

                throw ParseException("Unable to find message: $arg")
            }

            try {
                channel.getMessage(Snowflake(arg))
            } catch (e: NumberFormatException) {
                throw ParseException("Value '$arg' is not a valid message ID.")
            }
        }
    }
}

