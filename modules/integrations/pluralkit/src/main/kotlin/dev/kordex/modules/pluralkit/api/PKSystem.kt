/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
