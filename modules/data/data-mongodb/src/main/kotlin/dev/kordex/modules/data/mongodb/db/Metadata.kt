/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.data.mongodb.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.codecs.kotlinx.KotlinSerializerCodec

@Serializable
@Suppress("DataClassContainsFunctions", "DataClassShouldBeImmutable")
internal data class Metadata(
	@Contextual
	override val _id: String,

	var version: Int,
) : Entity<String> {

	suspend inline fun save(): UpdateResult =
		save(this)

	companion object {
		val codec = KotlinSerializerCodec.create<Metadata>()

		const val COLLECTION_NAME: String = "metadata"

		private val Filters = object {
			fun byId(id: String) =
				eq(Metadata::_id.name, id)
		}

		private val collection by lazy {
			Database.getCollection<Metadata>(COLLECTION_NAME)
		}

		suspend fun get(id: String): Int? =
			collection
				.find<Metadata>(Filters.byId(id))
				.limit(1)
				.firstOrNull()
				?.version

		suspend fun set(id: String, version: Int): UpdateResult =
			save(Metadata(id, version))

		suspend fun save(document: Metadata): UpdateResult =
			collection
				.replaceOne(
					Filters.byId(document._id),
					document,
					ReplaceOptions().upsert(true)
				)
	}
}
