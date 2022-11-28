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

package com.kotlindiscord.kord.extensions.modules.extra.mappings.stroage

import com.kotlindiscord.kord.extensions.storage.Data
import dev.kord.common.entity.Snowflake
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
@Suppress("DataClassShouldBeImmutable")
data class MappingsConfig(
    @TomlComment(
        "Category IDs to explicitly allow mappings commands within - leave empty for all."
    )
    var allowedCategories: List<Snowflake> = listOf(),

    @TomlComment(
        "Category IDs to explicitly disallow mappings commands within - leave empty for none."
    )
    var bannedCategories: List<Snowflake> = listOf(),

    @TomlComment(
        "Channel IDs to explicitly allow mappings commands within - leave empty for all."
    )
    var allowedChannels: List<Snowflake> = listOf(),

    @TomlComment(
        "Channel IDs to explicitly disallow mappings commands within - leave empty for none."
    )
    var bannedChannels: List<Snowflake> = listOf(),

    @TomlComment(
        "Guild IDs to explicitly allow mappings commands within - leave empty for all."
    )
    var allowedGuilds: List<Snowflake> = listOf(),

    @TomlComment(
        "Guild IDs to explicitly disallow mappings commands within - leave empty for none."
    )
    var bannedGuilds: List<Snowflake> = listOf(),

    @TomlComment(
        "Which namespaces to allow lookups for - 'hashed-mojang', 'legacy-yarn', 'plasma', 'quilt-mappings', " +
            "'mcp', 'mojang, 'yarn' or 'yarrn'"
    )
    var namespaces: List<String> =
        listOf("hashed-mojang", "legacy-yarn", "plasma", "quilt-mappings", "mcp", "mojang", "yarn", "yarrn"),

    @TomlComment(
        "How long to wait before closing mappings paginators (in seconds), defaults to 5 mins"
    )
    var timeout: String = "300",
) : Data
