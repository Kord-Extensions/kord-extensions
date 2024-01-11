/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.modules.extra.welcome.enums

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ThreadListType(val humanReadable: String) {
	@SerialName("active")
	ACTIVE("Active"),

	@SerialName("newest")
	NEWEST("Recently Created")
}
