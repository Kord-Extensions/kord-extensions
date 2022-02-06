/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.phishing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a Sinking Yachts domain change object.
 *
 * @property type Domain change type - add or delete
 * @property domains Set of domains that this change concerns
 */
@Serializable
data class DomainChange(
    val type: DomainChangeType,
    val domains: Set<String>
)

/**
 * Enum representing domain change types.
 *
 * @property readableName Readable name, for logging.
 */
@Serializable
enum class DomainChangeType(val readableName: String) {
    @SerialName("add")
    Add("added"),

    @SerialName("delete")
    Delete("deleted")
}
