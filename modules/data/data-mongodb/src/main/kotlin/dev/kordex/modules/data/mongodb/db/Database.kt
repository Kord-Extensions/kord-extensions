/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.data.mongodb.db

import com.mongodb.MongoClientSettings
import com.mongodb.MongoException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import dev.kordex.modules.data.mongodb.MONGODB_URI
import dev.kordex.modules.data.mongodb.kordExCodecRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.bson.BsonInt64
import org.bson.Document
import org.bson.codecs.configuration.CodecRegistries

internal object Database {
	private val logger = KotlinLogging.logger {}
	private val client = MongoClient.create(MONGODB_URI)

	private val codecRegistry = CodecRegistries.fromRegistries(
		kordExCodecRegistry,
		MongoClientSettings.getDefaultCodecRegistry(),
	)

	val db: MongoDatabase = client.getDatabase("kordex-data")

	@Throws(MongoException::class)
	suspend fun setup() {
		val command = Document("ping", BsonInt64(1))

		db.runCommand(command)

		logger.info { "Connected to database." }

		Migrations.migrate()
	}

	inline fun <reified T : Any> getCollection(name: String): MongoCollection<T> =
		db
			.getCollection<T>(name)
			.withCodecRegistry(codecRegistry)
}
