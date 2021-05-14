package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.guild
import com.kotlindiscord.kord.extensions.commands.converters.guildList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Guild
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
@OptIn(KordPreview::class)
public class GuildConverter(
    override var validator: (suspend Argument<*>.(Guild) -> Unit)? = null
) : SingleConverter<Guild>() {
    override val signatureTypeString: String = "converters.guild.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val guild = findGuild(arg)
            ?: throw CommandException(
                context.translate("converters.guild.error.missing", replacements = arrayOf(arg))
            )

        parsed = guild
        return true
    }

    private suspend fun findGuild(arg: String): Guild? =
        try { // Try for a guild ID first
            val id = Snowflake(arg)

            kord.getGuild(id)
        } catch (e: NumberFormatException) { // It's not an ID, let's try the name
            kord.guilds.firstOrNull { it.name.equals(arg, true) }
        }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
