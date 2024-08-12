/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.storage

import kotlinx.serialization.Serializable

/**
 * Sealed class representing the two types of storage - configuration and data.
 *
 * @property type Human-readable storage type name.
 */
@Serializable(with = StorageTypeSerializer::class)
public enum class StorageType(public val type: String) {
	Config("config"),
	Data("data")
}
