/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty",)

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PKMemberPrivacy(
    val visibility: Boolean,

    @SerialName("name_privacy")
    val namePrivacy: Boolean,

    @SerialName("description_privacy")
    val descriptionPrivacy: Boolean,

    @SerialName("birthday_privacy")
    val birthdayPrivacy: Boolean,

    @SerialName("pronoun_privacy")
    val pronounPrivacy: Boolean,

    @SerialName("avatar_privacy")
    val avatarPrivacy: Boolean,

    @SerialName("metadata_privacy")
    val metadataPrivacy: Boolean,
)
