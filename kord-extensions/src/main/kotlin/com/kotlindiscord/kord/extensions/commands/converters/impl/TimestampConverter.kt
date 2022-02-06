/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import com.kotlindiscord.kord.extensions.time.TimestampType
import com.kotlindiscord.kord.extensions.time.toDiscord
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import kotlinx.datetime.Instant

private const val TIMESTAMP_PREFIX = "<t:"
private const val TIMESTAMP_SUFFIX = ">"

/**
 * Argument converter for discord-formatted timestamp arguments.
 */
@Converter(
    "timestamp",

    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
@OptIn(KordPreview::class)
public class TimestampConverter(
    override var validator: Validator<FormattedTimestamp> = null
) : SingleConverter<FormattedTimestamp>() {
    override val signatureTypeString: String = "converters.timestamp.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false
        this.parsed = parseFromString(arg) ?: throw DiscordRelayedException(
            context.translate(
                "converters.timestamp.error.invalid",
                replacements = arrayOf(arg)
            )
        )

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false
        this.parsed = parseFromString(optionValue) ?: throw DiscordRelayedException(
            context.translate(
                "converters.timestamp.error.invalid",
                replacements = arrayOf(optionValue)
            )
        )

        return true
    }

    internal companion object {
        internal fun parseFromString(string: String): FormattedTimestamp? {
            if (string.startsWith(TIMESTAMP_PREFIX) && string.endsWith(TIMESTAMP_SUFFIX)) {
                val inner = string.removeSurrounding(TIMESTAMP_PREFIX, TIMESTAMP_SUFFIX).split(":")
                val epochSeconds = inner.getOrNull(0)
                val format = inner.getOrNull(1)

                return FormattedTimestamp(
                    Instant.fromEpochSeconds(epochSeconds?.toLongOrNull() ?: return null),
                    TimestampType.fromFormatSpecifier(format) ?: return null
                )
            } else {
                return null
            }
        }
    }
}

/**
 * Container class for a timestamp and format, as expected by Discord.
 *
 * @param instant The timestamp this represents
 * @param format Which format to display the timestamp in
 */
public data class FormattedTimestamp(val instant: Instant, val format: TimestampType) {
    /**
     * Format the timestamp using the format into Discord's special format.
     */
    public fun toDiscord(): String = instant.toDiscord(format)
}
