@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.parser.Arguments
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
    private var requiredGuild: (suspend () -> Snowflake)? = null,
    override var validator: (suspend Argument<*>.(Message) -> Unit)? = null
) : SingleConverter<Message>() {
    override val signatureTypeString: String = "converters.message.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val message = findMessage(arg, context)

        parsed = message
        return true
    }

    private suspend fun findMessage(arg: String, context: CommandContext): Message {
        val requiredGid = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id

        return if (arg.startsWith("https://")) { // It's a message URL
            @Suppress("MagicNumber")
            val split = arg.substring(8).split("/").takeLast(3)

            @Suppress("MagicNumber")
            if (split.size < 3) {
                throw CommandException(
                    context.translate("converters.message.error.invalidUrl", replacements = arrayOf(arg))
                )
            }

            @Suppress("MagicNumber")
            val gid = try {
                Snowflake(split[2])
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate("converters.message.error.invalidGuildId", replacements = arrayOf(split[2]))
                )
            }

            if (requireGuild && requiredGid != gid) {
                logger.debug { "Matching guild ($requiredGid) required, but guild ($gid) doesn't match." }

                errorNoMessage(arg, context)
            }

            @Suppress("MagicNumber")
            val cid = try {
                Snowflake(split[3])
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate(
                        "converters.message.error.invalidChannelId",
                        replacements = arrayOf(split[3])
                    )
                )
            }

            val channel = kord.getGuild(gid)?.getChannel(cid)

            if (channel == null) {
                logger.debug { "Unable to find channel ($cid) for guild ($gid)." }

                errorNoMessage(arg, context)
            }

            if (channel !is GuildMessageChannel) {
                logger.debug { "Specified channel ($cid) is not a guild message channel." }

                errorNoMessage(arg, context)
            }

            @Suppress("MagicNumber")
            val mid = try {
                Snowflake(split[4])
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate(
                        "converters.message.error.invalidMessageId",
                        replacements = arrayOf(split[4])
                    )
                )
            }

            try {
                channel.getMessage(mid)
            } catch (e: EntityNotFoundException) {
                errorNoMessage(mid.asString, context)
            }
        } else { // Try a message ID
            val channel = context.getChannel()

            if (channel !is GuildMessageChannel && channel !is DmChannel) {
                logger.debug { "Current channel is not a guild message channel or DM channel." }

                errorNoMessage(arg, context)
            }

            if (channel !is MessageChannel) {
                logger.debug { "Current channel is not a message channel, so it can't contain messages." }

                errorNoMessage(arg, context)
            }

            try {
                channel.getMessage(Snowflake(arg))
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate(
                        "converters.message.error.invalidMessageId",
                        replacements = arrayOf(arg)
                    )
                )
            } catch (e: EntityNotFoundException) {
                errorNoMessage(arg, context)
            }
        }
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    private suspend fun errorNoMessage(arg: String, context: CommandContext): Nothing {
        throw CommandException(context.translate("converters.message.error.missing", replacements = arrayOf(arg)))
    }
}

/**
 * Create a message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.message(
    displayName: String,
    description: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Message) -> Unit)? = null,
): SingleConverter<Message> = arg(displayName, description, MessageConverter(requireGuild, requiredGuild, validator))

/**
 * Create an optional message converter, for single arguments.
 *
 * @see MessageConverter
 */
public fun Arguments.optionalMessage(
    displayName: String,
    description: String,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Message?) -> Unit)? = null,
): OptionalConverter<Message?> =
    arg(
        displayName,
        description,
        MessageConverter(requireGuild, requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a message converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see MessageConverter
 */
public fun Arguments.messageList(
    displayName: String,
    description: String,
    required: Boolean = true,
    requireGuild: Boolean = false,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Message>) -> Unit)? = null,
): MultiConverter<Message> =
    arg(
        displayName,
        description,
        MessageConverter(requireGuild, requiredGuild)
            .toMulti(required, signatureTypeString = "messages", nestedValidator = validator)
    )
