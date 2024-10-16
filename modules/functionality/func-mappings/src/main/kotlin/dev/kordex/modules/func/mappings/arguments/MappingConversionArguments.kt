/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kord.common.entity.Snowflake
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.optionalEnumChoice
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.utils.getLocale
import dev.kordex.core.utils.suggestStringMap
import dev.kordex.modules.func.mappings.enums.Channels
import dev.kordex.modules.func.mappings.i18n.generated.MappingsTranslations
import dev.kordex.modules.func.mappings.utils.autocompleteVersions
import dev.kordex.modules.func.mappings.utils.toNamespace
import me.shedaniel.linkie.utils.tryToVersion

/**
 * Arguments for class, field, and method conversion commands.
 */
@Suppress("UndocumentedPublicProperty")
class MappingConversionArguments(enabledNamespaces: suspend (Snowflake?) -> Map<String, String>) : Arguments() {
	val query by string {
		name = MappingsTranslations.Argument.Query.name
		description = MappingsTranslations.Argument.Query.description
	}

	val inputNamespace by string {
		name = MappingsTranslations.Argument.Input.name
		description = MappingsTranslations.Argument.Input.description

		autoComplete {
			val guildId = command.data.guildId.value
			val values = enabledNamespaces(guildId)

			suggestStringMap(values)
		}

		@Suppress("UnnecessaryParentheses")
		validate {
			failIf(MappingsTranslations.Argument.namespaceValidationError) {
				context.getGuild() != null && value !in enabledNamespaces(context.getGuild()!!.id)
			}
		}
	}

	val outputNamespace by string {
		name = MappingsTranslations.Argument.Output.name
		description = MappingsTranslations.Argument.Output.description

		autoComplete {
			val guildId = command.data.guildId.value
			val values = enabledNamespaces(guildId)
			suggestStringMap(values)
		}

		@Suppress("UnnecessaryParentheses")
		validate {
			failIf(MappingsTranslations.Argument.namespaceValidationError) {
				context.getGuild() != null && value !in enabledNamespaces(context.getGuild()!!.id)
			}
		}
	}

	val version by optionalString {
		name = MappingsTranslations.Argument.Version.name
		description = MappingsTranslations.Argument.Version.description

		autocompleteVersions { event ->
			val inputNamespace = command.options["input"]?.value?.toString()?.toNamespace(event.getLocale())
			val outputNamespace = command.options["output"]?.value?.toString()?.toNamespace(event.getLocale())

			if (inputNamespace == null || outputNamespace == null) {
				emptyList()
			} else {
				inputNamespace.getAllVersions().toSet().intersect(outputNamespace.getAllVersions().toSet())
					.sortedByDescending { it.tryToVersion() }
			}
		}
	}

	val inputChannel by optionalEnumChoice<Channels> {
		name = MappingsTranslations.Argument.InputChannel.name
		description = MappingsTranslations.Argument.InputChannel.description
		typeName = MappingsTranslations.Argument.MappingsChannel.typeName
	}

	val outputChannel by optionalEnumChoice<Channels> {
		name = MappingsTranslations.Argument.OutputChannel.name
		description = MappingsTranslations.Argument.OutputChannel.description
		typeName = MappingsTranslations.Argument.MappingsChannel.typeName
	}
}
