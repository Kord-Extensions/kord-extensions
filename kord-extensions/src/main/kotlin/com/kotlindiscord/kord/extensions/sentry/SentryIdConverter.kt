@file:OptIn(KordPreview::class)

package com.kotlindiscord.kord.extensions.sentry

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import io.sentry.protocol.SentryId

/**
 * Argument converter for Sentry event ID arguments.
 *
 * @see sentryId
 * @see sentryIdList
 */
public class SentryIdConverter : SingleConverter<SentryId>() {
    override val signatureTypeString: String = "extensions.sentry.converter.sentryId.signatureType"

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
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false

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
