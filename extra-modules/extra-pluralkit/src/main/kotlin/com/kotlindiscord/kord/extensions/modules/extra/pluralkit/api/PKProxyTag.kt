/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction", "UndocumentedPublicProperty")

package com.kotlindiscord.kord.extensions.modules.extra.pluralkit.api

import kotlinx.serialization.Serializable

@Serializable
data class PKProxyTag(
	val prefix: String?,
	val suffix: String?,
)
