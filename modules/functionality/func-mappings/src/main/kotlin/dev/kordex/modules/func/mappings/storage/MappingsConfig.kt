/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress(
	"UnderscoresInNumericLiterals",
	"UndocumentedPublicClass",
	"UndocumentedPublicFunction",
	"UndocumentedPublicProperty",
)

package dev.kordex.modules.func.mappings.storage

import dev.kordex.core.storage.Data
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment
import net.peanuuutz.tomlkt.TomlInteger

@Serializable
@Suppress("DataClassShouldBeImmutable")
data class MappingsConfig(
	@TomlComment(
		"Which namespaces to allow conversions for - 'barn', 'feather', 'hashed-mojang', 'legacy-yarn'," +
			"'plasma', 'quilt-mappings', 'mcp', 'mojang', 'srg-mojang', 'yarn' or 'yarrn'"
	)
	var namespaces: List<String> =
		listOf(
			"barn",
			"feather",
			"hashed-mojang",
			"legacy-yarn",
			"mcp",
			"mojang",
			"plasma",
			"quilt-mappings",
			"srg-mojang",
			"yarn",
			"yarrn",
		),

	@TomlComment(
		"How long to wait before closing mappings paginators (in seconds), defaults to 5 mins"
	)
	@TomlInteger(TomlInteger.Base.DEC)
	var timeout: Int = 300,
) : Data
