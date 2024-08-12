/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.types

import kotlinx.serialization.Serializable

@Serializable(with = IdentifierSerializer::class)
public data class Identifier(public val namespace: String, public val id: String) {
	init {
		if (":" in namespace || ":" in id) {
			error("Namespace and ID must not contain a colon character (:)")
		}
	}

	override fun toString(): String =
		"$namespace:$id"

	// Generated methods

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false

		other as Identifier

		if (namespace != other.namespace) return false
		if (id != other.id) return false

		return true
	}

	override fun hashCode(): Int {
		var result = namespace.hashCode()
		result = 31 * result + id.hashCode()
		return result
	}
}

public fun Identifier(identifier: String): Identifier {
	if (identifier.count { it == ':' } != 1) {
		error("Identifiers must contain exactly one colon character (:), separating the namespace and ID.")
	}

	val parts = identifier.split(":", limit = 2)

	return Identifier(parts.first(), parts.last())
}
