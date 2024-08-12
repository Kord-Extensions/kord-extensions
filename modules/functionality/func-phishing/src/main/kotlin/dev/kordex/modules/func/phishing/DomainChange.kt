/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.phishing

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
	val domains: Set<String>,
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
