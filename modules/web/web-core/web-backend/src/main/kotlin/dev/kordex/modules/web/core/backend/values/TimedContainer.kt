/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.values

import dev.kordex.modules.web.core.backend.values.serializers.TimedContainerSerializer
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable(with = TimedContainerSerializer::class)
public data class TimedContainer<V : Any?>(
	val value: V,
	val time: Instant,
)
