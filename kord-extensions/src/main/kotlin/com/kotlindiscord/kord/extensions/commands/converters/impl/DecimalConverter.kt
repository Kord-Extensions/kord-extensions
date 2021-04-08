package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.ExtensibleBot
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.decimal
import com.kotlindiscord.kord.extensions.commands.converters.decimalList
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Argument converter for decimal arguments, converting them into [Double].
 *
 * @see decimal
 * @see decimalList
 */
@OptIn(KordPreview::class)
public class DecimalConverter(
    override var validator: (suspend (Double) -> Unit)? = null
) : SingleConverter<Double>() {
    override val signatureTypeString: String = "decimal"

    override suspend fun parse(arg: String, context: CommandContext, bot: ExtensibleBot): Boolean {
        try {
            this.parsed = arg.toDouble()
        } catch (e: NumberFormatException) {
            throw CommandException(
                "Value '$arg' is not a valid decimal number."
            )
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
