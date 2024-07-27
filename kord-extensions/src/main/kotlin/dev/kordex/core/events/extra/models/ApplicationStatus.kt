/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
