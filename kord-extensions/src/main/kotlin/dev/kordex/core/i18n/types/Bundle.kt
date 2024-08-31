/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.i18n.types

import kotlinx.serialization.Serializable

@Serializable
public data class Bundle(
	val name: String
) {
	override fun toString(): String =
		"Bundle \"$name\""
}
