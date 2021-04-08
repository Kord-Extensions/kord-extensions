package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.enum
import com.kotlindiscord.kord.extensions.commands.converters.enumList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for arbitrary enum arguments.
 *
 * As this converter is generic, it takes a getter lambda. You can either provide one yourself, or use the default
 * one via the provided extension functions - the default getter simply checks for case-insensitive matches on enum
 * names.
 *
 * @see enum
 * @see enumList
 */
@OptIn(KordPreview::class)
public class EnumConverter<E : Enum<E>>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    override var validator: (suspend (E) -> Unit)? = null
) : SingleConverter<E>() {
    override val signatureTypeString: String = typeName

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            parsed = getter.invoke(arg) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
