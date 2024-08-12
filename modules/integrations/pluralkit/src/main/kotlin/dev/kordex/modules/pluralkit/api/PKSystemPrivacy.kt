/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")

package dev.kordex.modules.pluralkit.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PKSystemPrivacy(
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
