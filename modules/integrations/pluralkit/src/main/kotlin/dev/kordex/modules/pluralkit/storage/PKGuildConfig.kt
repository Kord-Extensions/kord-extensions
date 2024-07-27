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
