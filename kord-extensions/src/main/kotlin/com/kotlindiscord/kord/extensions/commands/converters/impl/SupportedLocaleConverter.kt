@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

@file:Suppress("MagicNumber", "RethrowCaughtException", "TooGenericExceptionCaught")

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.i18n.SupportedLocales
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import java.util.*

/**
 * Argument converter for supported locale, converting them into [Locale] objects.
 *
 * This converter only supports locales defined in [com.kotlindiscord.kord.extensions.i18n.SupportedLocales]. It's
 * intended for use with commands that allow users to specify what locale they want the bot to use when interacting
 * with them, rather than a more general converter.
 *
 * If the locale you want to use isn't supported yet, feel free to contribute translations for it to
 * [our CrowdIn project](https://crowdin.com/project/kordex).
 */
@Converter(
    "supportedLocale",
    types = [ConverterType.DEFAULTING, ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE],
)
@OptIn(KordPreview::class)
public class SupportedLocaleConverter(
    override var validator: Validator<Locale> = null
) : SingleConverter<Locale>() {
    override val signatureTypeString: String = "converters.supportedLocale.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        this.parsed = SupportedLocales.ALL_LOCALES[arg.lowercase().trim()] ?: throw DiscordRelayedException(
            context.translate("converters.supportedLocale.error.unknown", replacements = arrayOf(arg))
        )

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
