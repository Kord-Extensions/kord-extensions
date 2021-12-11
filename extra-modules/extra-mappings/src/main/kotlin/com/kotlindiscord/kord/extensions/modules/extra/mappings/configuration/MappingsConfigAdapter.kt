/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.mappings.configuration

import com.kotlindiscord.kord.extensions.modules.extra.mappings.enums.YarnChannels
import dev.kord.common.entity.Snowflake

/**
 * Simple config adapter interface, which you can implement yourself if you need some kind of alternative config
 * backend.
 */
interface MappingsConfigAdapter {
    /** Get a list of category IDs mappings commands are explicitly allowed in. **/
    suspend fun getAllowedCategories(): List<Snowflake>

    /** Get a list of category IDs mappings commands are explicitly disallowed in. **/
    suspend fun getBannedCategories(): List<Snowflake>

    /** Get a list of channel IDs mappings commands are explicitly allowed in. **/
    suspend fun getAllowedChannels(): List<Snowflake>

    /** Get a list of channel IDs mappings commands are explicitly disallowed in. **/
    suspend fun getBannedChannels(): List<Snowflake>

    /** Get a list of guild IDs mappings commands are explicitly allowed in. **/
    suspend fun getAllowedGuilds(): List<Snowflake>

    /** Get a list of guild IDs mappings commands are explicitly disallowed in. **/
    suspend fun getBannedGuilds(): List<Snowflake>

    /** Get a list of enabled mappings namespaces. **/
    suspend fun getEnabledNamespaces(): List<String>

    /** Get a list of enabled extra Yarn channels. **/
    suspend fun getExtraYarnChannels(): List<YarnChannels>

    /** Check whether a Yarn channel is enabled. **/
    suspend fun yarnChannelEnabled(channel: YarnChannels): Boolean = when (channel) {
        YarnChannels.OFFICIAL -> true
        YarnChannels.SNAPSHOT -> true

        else -> getExtraYarnChannels().contains(channel)
    }

    /** Get the paginator timeout, in seconds. **/
    suspend fun getTimeout(): Long
}
