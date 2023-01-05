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

package com.kotlindiscord.kord.extensions.modules.extra.mappings.storage

import com.kotlindiscord.kord.extensions.storage.Data
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment
import net.peanuuutz.tomlkt.TomlInteger

@Serializable
@Suppress("DataClassShouldBeImmutable")
data class MappingsConfig(
    @TomlComment(
        "Which namespaces to allow conversions for - 'hashed-mojang', 'legacy-yarn', 'plasma', 'quilt-mappings', " +
            "'mcp', 'mojang, 'yarn' or 'yarrn'"
    )
    var namespaces: List<String> =
        listOf("hashed-mojang", "legacy-yarn", "plasma", "quilt-mappings", "mcp", "mojang", "yarn", "yarrn"),

    @TomlComment(
        "How long to wait before closing mappings paginators (in seconds), defaults to 5 mins"
    )
    @TomlInteger(TomlInteger.Base.DEC)
    var timeout: Int = 300,
) : Data
