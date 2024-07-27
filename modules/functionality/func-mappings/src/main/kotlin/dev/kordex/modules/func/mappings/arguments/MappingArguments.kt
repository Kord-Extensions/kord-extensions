/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.func.mappings.arguments

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.modules.func.mappings.converters.optionalMappingsVersion
import dev.kordex.modules.func.mappings.utils.autocompleteVersions
import me.shedaniel.linkie.Namespace

/**
 * Arguments base for mapping commands.
 */
@Suppress("UndocumentedPublicProperty")
open class MappingArguments(val namespace: Namespace) : Arguments() {
	private val versions by lazy { namespace.getAllSortedVersions() }

	val query by string {
		name = "query"
		description = "Name to query mappings for"
	}

	val version by optionalMappingsVersion {
		name = "version"
		description = "Minecraft version to use for this query"

		namespace(namespace)

		autocompleteVersions { versions }
	}
}
