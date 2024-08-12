/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events.extra.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public enum class ApplicationStatus {
	@SerialName("APPROVED")
	Approved,

	@SerialName("REJECTED")
	Rejected,

	@SerialName("SUBMITTED")
	Submitted
}
