/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(
    FlowPreview::class,
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.utils.translate
import dev.kord.common.entity.ChannelType
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.Channel
import dev.kord.core.entity.channel.GuildChannel
import dev.kord.core.entity.interaction.ChannelOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.ChannelBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filter
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
 * * `this` to refer to the current channel
 *
 * @param requireSameGuild Whether to require that the channel passed is on the same guild as the message.
 * @param requiredGuild Lambda returning a specific guild to require the channel to be in, if needed.
 */
@Converter(
    "channel",

    types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],

    imports = ["dev.kord.common.entity.ChannelType", "dev.kord.common.entity.Snowflake"],

    builderExtraStatements = [
        "/** Add a channel type to the set of types the given channel must match. **/",
        "public fun requireChannelType(type: ChannelType) {",
        "    requiredChannelTypes.add(type)",
        "}"
    ],

    builderFields = [
        "public var requireSameGuild: Boolean = true",
        "public var requiredGuild: (suspend () -> Snowflake)? = null",
        "public var requiredChannelTypes: MutableSet<ChannelType> = mutableSetOf()",
    ],
)
public class ChannelConverter(
    private val requireSameGuild: Boolean = true,
    private var requiredGuild: (suspend () -> Snowflake)? = null,
    private val requiredChannelTypes: Set<ChannelType> = setOf(),
    override var validator: Validator<Channel> = null
) : SingleConverter<Channel>() {
    override val signatureTypeString: String = "converters.channel.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        if (arg.equals("this", true)) {
            val channel = context.getChannel().asChannelOrNull()

            if (channel != null) {
                this.parsed = channel

                return true
            }
        }

        val channel: Channel = findChannel(arg, context) ?: throw DiscordRelayedException(
            context.translate(
                "converters.channel.error.missing", replacements = arrayOf(arg)
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
                throw DiscordRelayedException(
                    context.translate(
                        "converters.channel.error.invalid", replacements = arrayOf(id)
                    )
                )
            }
        } else {
            val string: String = if (arg.startsWith("#")) arg.substring(1) else arg

            try {
                kord.getChannel(Snowflake(string.toLong()))
            } catch (e: NumberFormatException) { // It's not a numeric ID, so let's try a channel name
                kord.guilds
                    .flatMapConcat { it.channels }
                    .filter { it.name.equals(string, true) }
                    .filter {
                        if (requiredChannelTypes.isNotEmpty()) {
                            it.type in requiredChannelTypes
                        } else {
                            true
                        }
                    }
                    .toList()
                    .firstOrNull()
            }
        }

        channel ?: return null

        if (channel is GuildChannel && (requireSameGuild || requiredGuild != null)) {
            val guildId: Snowflake? = if (requiredGuild != null) requiredGuild!!.invoke() else context.getGuild()?.id

            if (requireSameGuild && channel.guildId != guildId) {
                return null  // Channel isn't in the right guild
            }
        }

        if (requiredChannelTypes.isNotEmpty() && channel.type !in requiredChannelTypes) {
            val locale = context.getLocale()

            throw DiscordRelayedException(
                context.translate(
                    "converters.channel.error.wrongType",
                    replacements = arrayOf(
                        channel.type,
                        requiredChannelTypes.joinToString { "**${it.translate(locale)}**" }
                    )
                )
            )
        }

        return channel
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        ChannelBuilder(arg.displayName, arg.description).apply {
            channelTypes = requiredChannelTypes.toList()

            required = true
        }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? ChannelOptionValue)?.resolvedObject ?: return false
        this.parsed = optionValue

        return true
    }
}
