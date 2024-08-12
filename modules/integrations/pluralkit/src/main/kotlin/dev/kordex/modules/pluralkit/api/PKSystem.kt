/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")

package dev.kordex.modules.pluralkit.api

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PKSystem(
	val id: String,
	val uuid: String,
	val name: String?, // PK docs are wrong
	val description: String?,
	val tag: String?,

	@SerialName("avatar_url")
	val avatarUrl: String?,

	val banner: String?,
	val color: String?, // PK docs are wrong
	val created: Instant,
	val timezone: String? = null,
	val privacy: PKSystemPrivacy?,
)
