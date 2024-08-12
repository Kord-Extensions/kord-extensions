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

package dev.kordex.modules.pluralkit.storage

import dev.kord.common.entity.Snowflake
import dev.kordex.core.storage.Data
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
@Suppress("DataClassShouldBeImmutable", "MagicNumber")
data class PKGuildConfig(
	@TomlComment(
		"Base URL to use when attempting to hit the PluralKit API, without the /vX used to specify the version."
	)
	var apiUrl: String = "https://api.pluralkit.me",

	@TomlComment(
		"The ID of the PluralKit instance to use, if not the default instance."
	)
	var botId: Snowflake = Snowflake(466378653216014359),

	@TomlComment(
		"Whether PluralKit integration should be enabled on this server."
	)
	var enabled: Boolean = true,
) : Data
