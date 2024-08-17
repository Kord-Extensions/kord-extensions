/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.datacollection

import dev.kordex.core.storage.Data
import dev.kordex.data.api.DataCollection
import dev.kordex.data.api.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import java.util.UUID

@Suppress("DataClassShouldBeImmutable")
@Serializable
public data class State(
	var lastLevel: DataCollection? = DataCollection.Standard,

	@Serializable(with = UUIDSerializer::class)
	var uuid: UUID? = null,
) : Data
