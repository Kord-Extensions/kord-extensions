package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.boolean
import com.kotlindiscord.kord.extensions.commands.converters.booleanList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.utils.parseBoolean
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.BooleanBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

/**
 * Argument converter for [Boolean] arguments.
 *
 * Truthiness is determined by the [parseBoolean] function.
 *
 * @see boolean
 * @see booleanList
 */
@OptIn(KordPreview::class)
public class BooleanConverter(
    override var validator: (suspend Argument<*>.(Boolean) -> Unit)? = null
) : SingleConverter<Boolean>() {
    public override val signatureTypeString: String = "converters.boolean.signatureType"
    public override val errorTypeString: String = "converters.boolean.errorType"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        val bool = arg.parseBoolean(context) ?: return false

        this.parsed = bool

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        BooleanBuilder(arg.displayName, arg.description).apply { required = true }
}
