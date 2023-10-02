/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.db

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.client.result.UpdateResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId

@Serializable
@Suppress("DataClassContainsFunctions", "DataClassShouldBeImmutable")
internal data class Metadata(
	@BsonId
	override val _id: String,

	var version: Int,
) : Entity<String> {
	suspend inline fun save(): UpdateResult =
		Companion.save(this)

	companion object {
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
