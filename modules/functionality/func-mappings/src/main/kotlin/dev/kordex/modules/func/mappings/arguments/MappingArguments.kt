/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.modules.func.mappings.converters.optionalMappingsVersion
import dev.kordex.modules.func.mappings.i18n.generated.MappingsTranslations
import dev.kordex.modules.func.mappings.utils.autocompleteVersions
import me.shedaniel.linkie.Namespace

/**
 * Arguments base for mapping commands.
 */
@Suppress("UndocumentedPublicProperty")
open class MappingArguments(val namespace: Namespace) : Arguments() {
	private val versions by lazy { namespace.getAllSortedVersions() }

	val query by string {
		name = MappingsTranslations.Argument.Query.name
		description = MappingsTranslations.Argument.Query.description
	}

	val version by optionalMappingsVersion {
		name = MappingsTranslations.Argument.Version.name
		description = MappingsTranslations.Argument.Version.description

		namespace(namespace)

		autocompleteVersions { versions }
	}
}
