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
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.rest.builder.interaction.ChannelBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
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
@OptIn(KordPreview::class)
public class ChannelConverter(
    private val requireSameGuild: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null,
    override var validator: (suspend Argument<*>.(Channel) -> Unit)? = null
) : SingleConverter<Channel>() {
    override val signatureTypeString: String = "converters.channel.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val channel = findChannel(arg, context)
            ?: throw CommandException(
                context.translate(
                    "converters.channel.error.missing",
                    replacements = arrayOf(arg)
                )
            )

        parsed = channel
        return true
    }

    private suspend fun findChannel(arg: String, context: CommandContext): Channel? {
        val channel: Channel? = if (arg.startsWith("<#") && arg.endsWith(">")) { // Channel mention
            val id = arg.substring(2, arg.length - 1)

            try {
                kord.getChannel(Snowflake(id.toLong()))
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate(
                        "converters.channel.error.invalid",
                        replacements = arrayOf(id)
                    )
                )
            }
        } else {
            val string = if (arg.startsWith("#")) arg.substring(1) else arg

            var foundChannel: Channel? = null

            try {
                foundChannel = kord.getChannel(Snowflake(string.toLong()))
            } catch (e: NumberFormatException) { // It's not a numeric ID, so let's try a channel name
                for (channelObject in kord.guilds.flatMapConcat { it.channels }.toList()) {
                    if (channelObject.name.equals(string, false)) {
                        foundChannel = channelObject
                    }
                }
            }

            foundChannel
        }

        if (channel is GuildChannel && (requireSameGuild || requiredGuild != null)) {
            val guildId = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id

            if (requireSameGuild && channel.guildId != guildId) {
                return null  // Channel isn't in the right guild
            }
        }

        return channel
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        ChannelBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create a channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.channel(
    displayName: String,
    description: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(Channel) -> Unit)? = null,
): SingleConverter<Channel> = arg(
    displayName,
    description,
    ChannelConverter(requireSameGuild, requiredGuild, validator)
)

/**
 * Create an optional channel argument converter, for single arguments.
 *
 * @see ChannelConverter
 */
public fun Arguments.optionalChannel(
    displayName: String,
    description: String,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(Channel?) -> Unit)? = null,
): OptionalConverter<Channel?> =
    arg(
        displayName,
        description,
        ChannelConverter(requireSameGuild, requiredGuild)
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a channel argument converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see ChannelConverter
 */
public fun Arguments.channelList(
    displayName: String,
    description: String,
    required: Boolean = true,
    requireSameGuild: Boolean = true,
    requiredGuild: (suspend () -> Snowflake)? = null,
    validator: (suspend Argument<*>.(List<Channel>) -> Unit)? = null,
): MultiConverter<Channel> = arg(
    displayName,
    description,
    ChannelConverter(requireSameGuild, requiredGuild)
        .toMulti(required, signatureTypeString = "channels", nestedValidator = validator)
)
