/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty",)

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PKMember(
    val id: String,
    val uuid: String,
    val name: String,

    @SerialName("display_name")
    val displayName: String?,

    val color: String?, // PK docs are wrong
    val birthday: String?,
    val pronouns: String?,

    @SerialName("avatar_url")
    val avatarUrl: String?,

    val banner: String?,
    val description: String?,
    val created: Instant?,

    @SerialName("proxy_tags")
    val proxyTags: List<PKProxyTag>,

    @SerialName("keep_proxy")
    val keepProxy: Boolean,
    val privacy: PKMemberPrivacy?,
)
