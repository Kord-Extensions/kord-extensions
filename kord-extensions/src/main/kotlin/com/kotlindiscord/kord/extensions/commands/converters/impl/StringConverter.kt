package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.string
import com.kotlindiscord.kord.extensions.commands.converters.stringList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Coalescing argument that simply returns the argument as it was given.
 *
 * The multi version of this converter (via [toMulti]) will consume all remaining arguments.
 *
 * @see string
 * @see stringList
 */
@OptIn(KordPreview::class)
public class StringConverter(
    override var validator: (suspend Argument<*>.(String) -> Unit)? = null
) : SingleConverter<String>() {
    override val signatureTypeString: String = "converters.string.signatureType"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
