package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.gitlab.kordlib.common.entity.Snowflake
import com.gitlab.kordlib.core.entity.Message
import com.gitlab.kordlib.core.entity.channel.DmChannel
import com.gitlab.kordlib.core.entity.channel.GuildMessageChannel
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.ParseException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.message
import com.kotlindiscord.kord.extensions.commands.converters.messageList
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Argument converter for discord [Message] arguments.
 *
 * This converter supports specifying messages by supplying:
 * * A Discord message jump link
 * * A message ID (it will be assumed that the message is in the current channel).
 *
 * @param requireGuild Whether to require messages to be in a specified guild.
 * @param requiredGuild Lambda returning a specific guild to require the member to be in. If omitted, defaults to the
 * guild the command was invoked in.
 *
 * @see message
 * @see messageList
 */
public class MessageConverter(
    private var requireGuild: Boolean = false,
    private var requiredGuild: (suspend () -> Snowflake)? = null
) : SingleConverter<Message>() {
    override val signatureTypeString: String = "message"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val message = findMessage(arg, context, bot)

        parsed = message
        return true
    }

    private suspend fun findMessage(arg: String, context: CommandContext, bot: ExtensibleBot): Message {
        val requiredGid = if (requiredGuild != null) requiredGuild!!.invoke() else context.event.guildId

        return if (arg.startsWith("https://")) { // It's a message URL
            @Suppress("MagicNumber")
            val split = arg.substring(8).split("/").takeLast(3)

            @Suppress("MagicNumber")
            if (split.size < 3) {
                throw ParseException("Invalid message url provided: <$arg>")
            }

            @Suppress("MagicNumber")
            val gid = try {
                Snowflake(split[2])
            } catch (e: NumberFormatException) {
                throw ParseException("Value '${split[2]}' is not a valid guild ID.")
            }

            if (requireGuild && requiredGid != gid) {
                logger.debug { "Matching guild ($requiredGid) required, but guild ($gid) doesn't match." }

                throw ParseException("Unable to find message: $arg")
            }

            @Suppress("MagicNumber")
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

            @Suppress("MagicNumber")
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
