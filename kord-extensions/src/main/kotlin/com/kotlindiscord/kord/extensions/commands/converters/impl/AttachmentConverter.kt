/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.commands.converters.impl

import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.modules.annotations.converters.Converter
import com.kotlindiscord.kord.extensions.modules.annotations.converters.ConverterType
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.core.entity.Attachment
import dev.kord.core.entity.interaction.AttachmentOptionValue
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.AttachmentBuilder
import dev.kord.rest.builder.interaction.OptionsBuilder

/**
 * Argument converter for Discord attachments.
 */
@Converter(
    "attachment",

    types = [ConverterType.OPTIONAL, ConverterType.SINGLE],
)
public class AttachmentConverter(
    override var validator: Validator<Attachment> = null
) : SingleConverter<Attachment>() {
    override val signatureTypeString: String = "converters.attachment.signatureType"

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean = false

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        AttachmentBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? AttachmentOptionValue)?.resolvedObject ?: return false
        this.parsed = optionValue

        return true
    }
}
