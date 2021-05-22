package com.kotlindiscord.kord.extensions.modules.extra.mappings.converters

import com.kotlindiscord.kord.extensions.CommandException
import com.kotlindiscord.kord.extensions.commands.CommandContext
import com.kotlindiscord.kord.extensions.commands.converters.SingleConverter
import com.kotlindiscord.kord.extensions.commands.parser.Argument
import dev.kord.rest.builder.interaction.OptionsBuilder
import dev.kord.rest.builder.interaction.StringChoiceBuilder
import me.shedaniel.linkie.MappingsContainer
import me.shedaniel.linkie.Namespace

/**
 * Argument converter for [MappingsContainer] objects based on mappings versions.
 */
class MappingsVersionConverter(
    private val namespaceGetter: suspend () -> Namespace,
    override var validator: (suspend Argument<*>.(MappingsContainer) -> Unit)? = null
) : SingleConverter<MappingsContainer>() {
    override val signatureTypeString: String = "version"
    override val showTypeInSignature: Boolean = false

    override suspend fun parse(arg: String, context: CommandContext): Boolean {
        val namespace = namespaceGetter.invoke()

        if (arg in namespace.getAllVersions()) {
            val version = namespace.getProvider(arg).getOrNull()

//            if (version == null) {
//                throw CommandException("Invalid ${namespace.id} version: `$arg`")
//
//                val created = namespace.createAndAdd(arg)
//
//                if (created != null) {
//                    this.parsed = created
//                } else {
//                    throw CommandException("Invalid ${namespace.id} version: `$arg`")
//                }
//            } else {
//                this.parsed = version
//            }

            if (version != null) {
                this.parsed = version

                return true
            }
        }

        throw CommandException("Invalid ${namespace.id} version: `$arg`")
    }

    override suspend fun toSlashOption(arg: Argument<*>): OptionsBuilder =
        StringChoiceBuilder(arg.displayName, arg.description).apply { required = true }
}
