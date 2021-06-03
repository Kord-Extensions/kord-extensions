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
import dev.kord.core.entity.GuildEmoji
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapNotNull

/**
 * Argument converter for Discord [GuildEmoji] arguments.
 *
 * This converter supports specifying emojis by supplying:
 *
 * * The actual emoji itself
 * * The emoji ID, either with or without surrounding colons
 * * The emoji name, either with or without surrounding colons -
 * the first matching emoji available to the bot will be used
 *
 * @see emoji
 * @see emojiList
 */
@OptIn(KordPreview::class)
public class EmojiConverter(
    override var validator: (suspend Argument<*>.(GuildEmoji) -> Unit)? = null
) : SingleConverter<GuildEmoji>() {
    override val signatureTypeString: String = "converters.emoji.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val emoji = findEmoji(arg, context)
            ?: throw CommandException(
                context.translate("converters.emoji.error.missing", replacements = arrayOf(arg))
            )

        parsed = emoji
        return true
    }

    private suspend fun findEmoji(arg: String, context: CommandContext): GuildEmoji? =
        if (arg.startsWith("<a:") || arg.startsWith("<:") && arg.endsWith('>')) { // Emoji mention
            val id = arg.substring(0, arg.length - 1).split(":").last()

            try {
                val snowflake = Snowflake(id)

                kord.guilds.mapNotNull {
                    it.getEmojiOrNull(snowflake)
                }.firstOrNull()
            } catch (e: NumberFormatException) {
                throw CommandException(
                    context.translate("converters.emoji.error.invalid", replacements = arrayOf(id))
                )
            }
        } else { // ID or name
            val name = if (arg.startsWith(":") && arg.endsWith(":")) arg.substring(1, arg.length - 1) else arg

            try {
                val snowflake = Snowflake(name)

                kord.guilds.mapNotNull {
                    it.getEmojiOrNull(snowflake)
                }.firstOrNull()
            } catch (e: NumberFormatException) {  // Not an ID, let's check names
                kord.guilds.mapNotNull {
                    it.emojis.first { emojiObj -> emojiObj.name?.lowercase().equals(name, true) }
                }.firstOrNull()
            }
        }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create an emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.emoji(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(GuildEmoji) -> Unit)? = null,
): SingleConverter<GuildEmoji> =
    arg(displayName, description, EmojiConverter(validator))

/**
 * Create an optional emoji converter, for single arguments.
 *
 * @see EmojiConverter
 */
public fun Arguments.optionalEmoji(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(GuildEmoji?) -> Unit)? = null,
): OptionalConverter<GuildEmoji?> =
    arg(
        displayName,
        description,
        EmojiConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create an emoji converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EmojiConverter
 */
public fun Arguments.emojiList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<GuildEmoji>) -> Unit)? = null,
): MultiConverter<GuildEmoji> =
    arg(
        displayName,
        description,
        EmojiConverter()
            .toMulti(required, signatureTypeString = "server emojis", nestedValidator = validator)
    )
