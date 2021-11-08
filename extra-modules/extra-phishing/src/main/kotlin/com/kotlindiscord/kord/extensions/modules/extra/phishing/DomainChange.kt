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

/** Enum representing domain change types. **/
@Serializable
enum class DomainChangeType {
    @SerialName("add")
    Add,

    @SerialName("delete")
    Delete
}
