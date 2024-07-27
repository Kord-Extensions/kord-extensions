/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kord.common.entity.Snowflake
import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.converters.impl.optionalEnumChoice
import dev.kordex.core.commands.converters.impl.optionalString
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.utils.suggestStringMap
import dev.kordex.modules.func.mappings.enums.Channels
import dev.kordex.modules.func.mappings.utils.autocompleteVersions
import dev.kordex.modules.func.mappings.utils.toNamespace
import me.shedaniel.linkie.utils.tryToVersion

/**
 * Arguments for class, field, and method conversion commands.
 */
@Suppress("UndocumentedPublicProperty")
class MappingConversionArguments(enabledNamespaces: suspend (Snowflake?) -> Map<String, String>) : Arguments() {
	val query by string {
		name = "query"
		description = "Name to query mappings for"
	}

	val inputNamespace by string {
		name = "input"
		description = "The namespace to convert from"

		autoComplete {
			val guildId = command.data.guildId.value
			val values = enabledNamespaces(guildId)

			suggestStringMap(values)
		}

		@Suppress("UnnecessaryParentheses")
		validate {
			failIf("Must be a valid namespace") {
				context.getGuild() != null && value !in enabledNamespaces(context.getGuild()!!.id)
			}
		}
	}

	val outputNamespace by string {
		name = "output"
		description = "The namespace to convert to"

		autoComplete {
			val guildId = command.data.guildId.value
			val values = enabledNamespaces(guildId)
			suggestStringMap(values)
		}

		@Suppress("UnnecessaryParentheses")
		validate {
			failIf("Must be a valid namespace") {
				context.getGuild() != null && value !in enabledNamespaces(context.getGuild()!!.id)
			}
		}
	}

	val version by optionalString {
		name = "version"
		description = "Minecraft version to use for this query"

		autocompleteVersions {
			val inputNamespace = command.options["input"]?.value?.toString()?.toNamespace()
			val outputNamespace = command.options["output"]?.value?.toString()?.toNamespace()

			if (inputNamespace == null || outputNamespace == null) {
				emptyList()
			} else {
				inputNamespace.getAllVersions().toSet().intersect(outputNamespace.getAllVersions().toSet())
					.sortedByDescending { it.tryToVersion() }
			}
		}
	}

	val inputChannel by optionalEnumChoice<Channels> {
		name = "input-channel"
		description = "The mappings channel to use for input"

		typeName = "official/snapshot"
	}

	val outputChannel by optionalEnumChoice<Channels> {
		name = "output-channel"
		description = "The mappings channel to use for output"

		typeName = "official/snapshot"
	}
}
