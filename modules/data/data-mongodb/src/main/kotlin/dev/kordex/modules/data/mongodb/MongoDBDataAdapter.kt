/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

@file:Suppress("UNCHECKED_CAST")
@file:OptIn(InternalSerializationApi::class)

package dev.kordex.modules.data.mongodb

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.ReplaceOptions
import com.mongodb.kotlin.client.coroutine.MongoCollection
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.about.CopyrightType
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.storage.Data
import dev.kordex.core.storage.DataAdapter
import dev.kordex.core.storage.StorageUnit
import dev.kordex.core.utils.MutableStringKeyedMap
import dev.kordex.modules.data.mongodb.db.AdaptedData
import dev.kordex.modules.data.mongodb.db.Database
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.bson.conversions.Bson
import org.koin.core.component.inject

private var copyrightAdded = false

/**
 * This class represents a MongoDB data adapter for storing and retrieving data using MongoDB as the underlying
 * database for data stored using storage units.
 *
 * Use the provided [mongoDB] function to add this to your bot, rather than directly referencing the constructor for
 * this class.
 */
public class MongoDBDataAdapter : DataAdapter<String>(), KordExKoinComponent {
	private val collectionCache: MutableStringKeyedMap<MongoCollection<AdaptedData>> = mutableMapOf()
	private val settings: ExtensibleBotBuilder by inject()

	init {
		if (!copyrightAdded) {
			settings.aboutBuilder.copyright(
				"MongoDB: Kotlin Driver",
				"Apache-2.0",
				CopyrightType.Library,
				"https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/"
			)

			settings.aboutBuilder.copyright(
				"MongDB: Kotlin BSONx",
				"Apache-2.0",
				CopyrightType.Library,
				"https://www.mongodb.com/docs/drivers/kotlin/coroutine/current/fundamentals/data-formats/serialization/"
			)

			copyrightAdded = true
		}
	}

	private fun StorageUnit<*>.getIdentifier(): String =
		buildString {
			append("${storageType.type}/")

			if (guild != null) append("guild-$guild/")
			if (channel != null) append("channel-$channel/")
			if (user != null) append("user-$user/")
			if (message != null) append("message-$message/")

			append(identifier)
		}

	private fun getCollection(namespace: String): MongoCollection<AdaptedData> {
		val collName = "data-$namespace"

		return collectionCache.getOrPut(collName) { Database.getCollection<AdaptedData>(collName) }
	}

	private fun constructQuery(unit: StorageUnit<*>): Bson =
		Filters.and(
			listOf(
				eq(AdaptedData::identifier.name, unit.identifier),

				eq(AdaptedData::type.name, unit.storageType),

				eq(AdaptedData::channel.name, unit.channel),
				eq(AdaptedData::guild.name, unit.guild),
				eq(AdaptedData::message.name, unit.message),
				eq(AdaptedData::user.name, unit.user)
			)
		)

	override suspend fun <R : Data> delete(unit: StorageUnit<R>): Boolean {
		removeFromCache(unit)

		val result = getCollection(unit.namespace)
			.deleteOne(constructQuery(unit))

		return result.deletedCount > 0
	}

	override suspend fun <R : Data> get(unit: StorageUnit<R>): R? {
		val dataId = unitCache[unit]

		if (dataId != null) {
			val data = dataCache[dataId]

			if (data != null) {
				return data as R
			}
		}

		return reload(unit)
	}

	override suspend fun <R : Data> reload(unit: StorageUnit<R>): R? {
		val dataId = unit.getIdentifier()
		val result = getCollection(unit.namespace)
			.find(constructQuery(unit)).limit(1).firstOrNull()?.data

		if (result != null) {
			dataCache[dataId] = Json.decodeFromString(unit.dataType.serializer(), result)
			unitCache[unit] = dataId
		}

		return dataCache[dataId] as R?
	}

	override suspend fun <R : Data> save(unit: StorageUnit<R>): R? {
		val data = get(unit) ?: return null

		getCollection(unit.namespace).replaceOne(
			eq(unit.getIdentifier()),

			AdaptedData(
				_id = unit.getIdentifier(),

				identifier = unit.identifier,

				type = unit.storageType,

				channel = unit.channel,
				guild = unit.guild,
				message = unit.message,
				user = unit.user,

				data = Json.encodeToString(unit.dataType.serializer(), data)
			),

			ReplaceOptions().upsert(true)
		)

		return data
	}

	override suspend fun <R : Data> save(unit: StorageUnit<R>, data: R): R {
		val dataId = unit.getIdentifier()

		dataCache[dataId] = data
		unitCache[unit] = dataId

		getCollection(unit.namespace).replaceOne(
			eq(unit.getIdentifier()),

			AdaptedData(
				_id = unit.getIdentifier(),

				identifier = unit.identifier,

				type = unit.storageType,

				channel = unit.channel,
				guild = unit.guild,
				message = unit.message,
				user = unit.user,

				data = Json.encodeToString(unit.dataType.serializer(), data)
			),

			ReplaceOptions().upsert(true)
		)

		return data
	}
}
