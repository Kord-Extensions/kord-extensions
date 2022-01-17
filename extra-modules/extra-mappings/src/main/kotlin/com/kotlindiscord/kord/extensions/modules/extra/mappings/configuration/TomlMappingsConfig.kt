/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration

import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec.CategoriesSpec
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec.ChannelsSpec
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec.GuildsSpec
import com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration.spec.SettingsSpec
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.Feature
import com.uchuhimo.konf.source.toml
import dev.kord.common.entity.Snowflake
import java.io.File

/**
 * Implementation of [MappingsConfigAdapter] backed by TOML files, system properties and env vars.
 *
 * For more information on how this works, see the README.
 */
class TomlMappingsConfig : MappingsConfigAdapter {
    private var config = Config {
        addSpec(CategoriesSpec)
        addSpec(ChannelsSpec)
        addSpec(GuildsSpec)
        addSpec(SettingsSpec)
    }
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource("kordex/mappings/default.toml")
        .from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.resource(
            "kordex/mappings/config.toml",
            optional = true
        )

    init {
        if (File("config/ext/mappings.toml").exists()) {
            config = config.from.enabled(Feature.FAIL_ON_UNKNOWN_PATH).toml.watchFile(
                "config/ext/mappings.toml",
                optional = true
            )
        }

        config = config
            .from.prefixed("KORDEX_MAPPINGS").env()
            .from.prefixed("kordex.mappings").systemProperties()
    }

    override suspend fun getAllowedCategories(): List<Snowflake> = config[CategoriesSpec.allowed]
    override suspend fun getBannedCategories(): List<Snowflake> = config[CategoriesSpec.banned]

    override suspend fun getAllowedChannels(): List<Snowflake> = config[ChannelsSpec.allowed]
    override suspend fun getBannedChannels(): List<Snowflake> = config[ChannelsSpec.banned]

    override suspend fun getAllowedGuilds(): List<Snowflake> = config[GuildsSpec.allowed]
    override suspend fun getBannedGuilds(): List<Snowflake> = config[GuildsSpec.banned]

    override suspend fun getEnabledNamespaces(): List<String> = config[SettingsSpec.namespaces]

    override suspend fun getTimeout(): Long = config[SettingsSpec.timeout]
}
