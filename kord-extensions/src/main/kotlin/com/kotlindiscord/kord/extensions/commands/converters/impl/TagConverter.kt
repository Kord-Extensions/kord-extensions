/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.ibm.icu.util.ULocale
import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.i18n.DEFAULT_KORDEX_BUNDLE
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.entity.ForumTag
import dev.kord.core.behavior.channel.asChannelOfOrNull
import dev.kord.core.entity.channel.ForumChannel
import dev.kord.core.entity.channel.thread.ThreadChannel
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for [ForumTag] arguments.
 *
 * Accepts a callable [channelGetter] property which may be used to extract a forum channel from another argument.
 */
@Converter(
    "tag",

    types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
    imports = ["dev.kord.core.entity.channel.ForumChannel"],

    builderFields = [
        "public var channelGetter: (suspend () -> ForumChannel?)? = null"
    ],
)

public class TagConverter(
    private var channelGetter: (suspend () -> ForumChannel?)? = null,
    override var validator: Validator<ForumTag> = null,
) : SingleConverter<ForumTag>() {
    public override val signatureTypeString: String = "converters.tag.signatureType"
    override val bundle: String = DEFAULT_KORDEX_BUNDLE

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val input: String = named ?: parser?.parseNext()?.data ?: return false

        this.parsed = getTag(input, context)

        return true
    }

    private suspend fun getTag(input: String, context: CommandContext): ForumTag {
        val tags: List<ForumTag> = getTags(context)
        val locale: ULocale = ULocale(context.getLocale().toString())

        val tag: ForumTag = tags.firstOrNull {
            it.name.equals(input, true)
        } ?: tags.firstOrNull {
            if (locale.isRightToLeft) {
                it.name.endsWith(input, true)
            } else {
                it.name.startsWith(input, true)
            }
        } ?: tags.firstOrNull {
            it.name.contains(input, true)
        } ?: throw DiscordRelayedException(
            context.translate(
                "converters.tag.error.unknownTag",
                replacements = arrayOf(input)
            )
        )

        return tag
    }

    private suspend fun getTags(context: CommandContext): List<ForumTag> {
        val channel: ForumChannel? = if (channelGetter != null) {
            channelGetter!!()
        } else {
            val thread = context.getChannel().asChannelOfOrNull<ThreadChannel>()

            thread?.parent?.asChannelOfOrNull<ForumChannel>()
        }

        if (channel == null) {
            throw DiscordRelayedException(
                context.translate(
                    if (channelGetter == null) {
                        "converters.tag.error.wrongChannelType"
                    } else {
                        "converters.tag.error.wrongChannelTypeWithGetter"
                    }
                )
            )
        }

        return channel.availableTags
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue: String = (option as? StringOptionValue)?.value ?: return false

        this.parsed = getTag(optionValue, context)

        return true
    }
}
