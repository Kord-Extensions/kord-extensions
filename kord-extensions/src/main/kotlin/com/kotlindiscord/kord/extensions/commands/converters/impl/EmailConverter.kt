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
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import org.apache.commons.validator.routines.EmailValidator

/**
 * Argument converter for email address arguments.
 *
 * @see email
 * @see emailList
 */
@OptIn(KordPreview::class)
public class EmailConverter(
    override var validator: (suspend Argument<*>.(String) -> Unit)? = null
) : SingleConverter<String>() {
    override val signatureTypeString: String = "converters.email.signatureType"

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        if (!EmailValidator.getInstance().isValid(arg)) {
            throw CommandException(
                context.translate("converters.email.error.invalid", replacements = arrayOf(arg))
            )
        }

        this.parsed = arg

        return true
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}

/**
 * Create an email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.email(
    displayName: String,
    description: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): SingleConverter<String> =
    arg(displayName, description, EmailConverter(validator))

/**
 * Create an optional email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.optionalEmail(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    validator: (suspend Argument<*>.(String?) -> Unit)? = null,
): OptionalConverter<String?> =
    arg(
        displayName,
        description,
        EmailConverter()
            .toOptional(outputError = outputError, nestedValidator = validator)
    )

/**
 * Create a defaulting email converter, for single arguments.
 *
 * @see EmailConverter
 */
public fun Arguments.defaultingEmail(
    displayName: String,
    description: String,
    defaultValue: String,
    validator: (suspend Argument<*>.(String) -> Unit)? = null,
): DefaultingConverter<String> =
    arg(
        displayName,
        description,
        EmailConverter()
            .toDefaulting(defaultValue, nestedValidator = validator)
    )

/**
 * Create an email converter, for lists of arguments.
 *
 * @param required Whether command parsing should fail if no arguments could be converted.
 *
 * @see EmailConverter
 */
public fun Arguments.emailList(
    displayName: String,
    description: String,
    required: Boolean = true,
    validator: (suspend Argument<*>.(List<String>) -> Unit)? = null,
): MultiConverter<String> =
    arg(
        displayName,
        description,
        EmailConverter()
            .toMulti(required, nestedValidator = validator)
    )
