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

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.storage

import com.kotlindiscord.kord.extensions.storage.Data
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
@Suppress("DataClassShouldBeImmutable", "MagicNumber")
data class PKGuildConfig(
    @TomlComment(
        "Base URL to use when attempting to hit the PluralKit API, without the /vX used to specify the version."
    )
    var apiUrl: String = "https://api.pluralkit.me/v2",

    @TomlComment(
        "The ID of the PluralKit instance to use, if not the default instance."
    )
    var botId: Snowflake = Snowflake(466378653216014359),

    @TomlComment(
        "Whether PluralKit integration should be enabled on this server."
    )
    var enabled: Boolean = true,
) : Data
