@file:OptIn(ConverterToOptional::class, KordPreview::class)

package com.kotlindiscord.kord.extensions.modules.extra.mappings.converters

import com.kotlindiscord.kord.extensions.DiscordRelayedException
import com.kotlindiscord.kord.extensions.commands.Argument
import com.kotlindiscord.kord.extensions.commands.Arguments
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.ConverterToOptional
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.converters.Validator
import com.kotlindiscord.kord.extensions.parser.StringParser
import dev.kord.common.annotation.KordPreview
import dev.kord.core.entity.interaction.OptionValue
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace

/**
 * Argument converter for [MappingsContainer] objects based on mappings versions.
 */
class MappingsVersionConverter(
    private val namespaceGetter: suspend () -> Namespace,
    override var validator: Validator<MappingsContainer> = null
) : SingleConverter<MappingsContainer>() {
    override val signatureTypeString: String = "version"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(parser: StringParser?, context: CommandContext, named: String?): Boolean {
        val arg: String = named ?: parser?.parseNext()?.data ?: return false

        val namespace = namespaceGetter.invoke()

        if (arg in namespace.getAllVersions()) {
            val version = namespace.getProvider(arg).getOrNull()

            if (version != null) {
                this.parsed = version

                return true
            }
        }

        throw DiscordRelayedException("Invalid ${namespace.id} version: `$arg`")
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }

    override suspend fun parseOption(context: CommandContext, option: OptionValue<*>): Boolean {
        val optionValue = (option as? OptionValue.StringOptionValue)?.value ?: return false
        val namespace = namespaceGetter.invoke()

        if (optionValue in namespace.getAllVersions()) {
            val version = namespace.getProvider(optionValue).getOrNull()

            if (version != null) {
                this.parsed = version

                return true
            }
        }

        throw DiscordRelayedException("Invalid ${namespace.id} version: `$optionValue`")
    }
}

/** Optional mappings version converter; see KordEx bundled functions for more info. **/
fun Arguments.optionalMappingsVersion(
    displayName: String,
    description: String,
    outputError: Boolean = false,
    namespace: Namespace,
    validator: Validator<MappingsContainer?> = null
) =
    arg(
        displayName,
        description,
        MappingsVersionConverter({ namespace })
            .toOptional(outputError = outputError, nestedValidator = validator)
    )
