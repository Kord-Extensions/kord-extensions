/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.i18n.DEFAULT_KORDEX_BUNDLE
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.core.entity.interaction.StringOptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import io.sentry.protocol.SentryId

/**
 * Argument converter for Sentry event ID arguments.
 *
 * @see sentryId
 * @see sentryIdList
 */
@Converter(
	"sentryId",
	types = [ConverterType.SINGLE, ConverterType.LIST, ConverterType.OPTIONAL]
)
public class SentryIdConverter(
	override var validator: Validator<SentryId> = null
) : SingleConverter<SentryId>() {
    override val signatureTypeString: String = "extensions.sentry.converter.sentryId.signatureType"
	override val bundle: String = DEFAULT_KORDEX_BUNDLE

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        try {
            this.parsed = SentryId(arg)
        } catch (e: IllegalArgumentException) {
            throw DiscordRelayedException(
                context.translate("extensions.sentry.converter.error.invalid", replacements = arrayOf(arg))
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? StringOptionValue)?.value ?: return false

        try {
            this.parsed = SentryId(optionValue)
        } catch (e: IllegalArgumentException) {
            throw DiscordRelayedException(
                context.translate("extensions.sentry.converter.error.invalid", replacements = arrayOf(optionValue))
            )
        }

        return true
    }
}
