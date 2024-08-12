/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
