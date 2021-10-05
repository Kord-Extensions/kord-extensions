@file:OptIn(
    KordPreview::class,
    ConverterToDefaulting::class,
    ConverterToMulti::class,
    ConverterToOptional::class
)

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.*
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import kotlinx.coroutines.flow.firstOrNull

/**
 * Argument converter for Discord [Guild] arguments.
 *
 * This converter supports specifying guilds by supplying:
 * * A guild ID
 * * The name of the guild - the first matching guild available to the bot will be used
 *
 * @see guild
 * @see guildList
 */
@Converter(
    "guild",

    types = [ConverterType.LIST, ConverterType.OPTIONAL, ConverterType.SINGLE]
)
@OptIn(KordPreview::class)
public class GuildConverter(
    override var validator: Validator<Guild> = null
) : SingleConverter<Guild>() {
    override val signatureTypeString: String = "converters.guild.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        this.parsed = findGuild(arg)
            ?: throw DiscordRelayedException(
                context.translate("converters.guild.error.missing", replacements = arrayOf(arg))
            )

        return true
    }

    private suspend fun findGuild(arg: String): Guild? =
        try { // Try for a guild ID first
            val id: Snowflake = Snowflake(arg)

            kord.getGuild(id)
        } catch (e: NumberFormatException) { // It's not an ID, let's try the name
            kord.guilds.firstOrNull { it.name.equals(arg, true) }
        }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false

        this.parsed = findGuild(optionValue)
            ?: throw DiscordRelayedException(
                context.translate("converters.guild.error.missing", replacements = arrayOf(optionValue))
            )

        return true
    }
}
