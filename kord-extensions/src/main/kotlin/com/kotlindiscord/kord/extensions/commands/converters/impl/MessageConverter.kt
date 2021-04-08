package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.message
import com.kotlindiscord.kord.extensions.commands.converters.messageList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Message
import dev.kord.core.entity.channel.DmChannel
import dev.kord.core.entity.channel.GuildMessageChannel
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.exception.EntityNotFoundException
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
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
@OptIn(KordPreview::class)
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
        val requiredGid = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id

        return if (arg.startsWith("https://")) { // It's a message URL
            @Suppress("MagicNumber")
            val split = arg.substring(8).split("/").takeLast(3)

            @Suppress("MagicNumber")
            if (split.size < 3) {
                throw CommandException("Invalid message url provided: <$arg>")
            }

            @Suppress("MagicNumber")
            val gid = try {
                Snowflake(split[2])
            } catch (e: NumberFormatException) {
                throw CommandException("Value '${split[2]}' is not a valid guild ID.")
            }

            if (requireGuild && requiredGid != gid) {
                logger.debug { "Matching guild ($requiredGid) required, but guild ($gid) doesn't match." }

                errorNoMessage(arg)
            }

            @Suppress("MagicNumber")
            val cid = try {
                Snowflake(split[3])
            } catch (e: NumberFormatException) {
                throw CommandException("Value '${split[3]}' is not a valid channel ID.")
            }

            val channel = bot.kord.getGuild(gid)?.getChannel(cid)

            if (channel == null) {
                logger.debug { "Unable to find channel ($cid) for guild ($gid)." }

                errorNoMessage(arg)
            }

            if (channel !is GuildMessageChannel) {
                logger.debug { "Specified channel ($cid) is not a guild message channel." }

                errorNoMessage(arg)
            }

            @Suppress("MagicNumber")
            val mid = try {
                Snowflake(split[4])
            } catch (e: NumberFormatException) {
                throw CommandException("Value '${split[4]}' is not a valid message ID.")
            }

            try {
                channel.getMessage(mid)
            } catch (e: EntityNotFoundException) {
                errorNoMessage(mid.asString)
            }
        } else { // Try a message ID
            val channel = context.getChannel()

            if (channel !is GuildMessageChannel && channel !is DmChannel) {
                logger.debug { "Current channel is not a guild message channel or DM channel." }

                errorNoMessage(arg)
            }

            if (channel !is MessageChannel) {
                logger.debug { "Current channel is not a message channel, so it can't contain messages." }

                errorNoMessage(arg)
            }

            try {
                channel.getMessage(Snowflake(arg))
            } catch (e: NumberFormatException) {
                throw CommandException("Value '$arg' is not a valid message ID.")
            } catch (e: EntityNotFoundException) {
                errorNoMessage(arg)
            }
        }
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    private fun errorNoMessage(arg: String): Nothing {
        throw CommandException("Unable to find message: $arg")
    }
}
