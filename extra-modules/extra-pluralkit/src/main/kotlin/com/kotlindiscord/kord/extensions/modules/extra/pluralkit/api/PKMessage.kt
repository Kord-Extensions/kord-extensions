/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty",)

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api

import dev.kord.common.entity.Snowflake
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class PKMessage(
    val timestamp: Instant,
    val id: Snowflake,
    val original: Snowflake,
    val sender: Snowflake,
    val channel: Snowflake,

    val system: PKSystem? = null,
    val member: PKMember? = null,
)
