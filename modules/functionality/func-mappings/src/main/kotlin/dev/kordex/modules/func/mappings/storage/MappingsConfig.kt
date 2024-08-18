/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
	@TomlInteger(TomlInteger.Base.Dec)
	var timeout: Int = 300,
) : Data
