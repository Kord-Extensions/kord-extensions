package com.kotlindiscord.kord.extensions.commands.slash.converters.impl

import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceConverter
import com.kotlindiscord.kord.extensions.commands.slash.converters.ChoiceEnum
import dev.kord.common.annotation.KordPreview
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder

/**
 * Choice converter for enum arguments. Supports mapping up to 25 choices to an enum type.
 *
 * All enums used for this must implement the [ChoiceEnum] interface.
 */
@OptIn(KordPreview::class)
public class EnumChoiceConverter<E>(
    typeName: String,
    private val getter: suspend (String) -> E?,
    choices: Array<E>,
    override var validator: (suspend Argument<*>.(E) -> Unit)? = null
) : ChoiceConverter<E>(choices.associateBy { it.readableName }) where E : Enum<E>, E : ChoiceEnum {
    override val signatureTypeString: String = typeName

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        try {
            parsed = getter.invoke(arg) ?: return false
        } catch (e: IllegalArgumentException) {
            return false
        }

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply {
            required = true

            this@EnumChoiceConverter.choices.forEach { choice(it.key, it.value.name) }
        }
}
