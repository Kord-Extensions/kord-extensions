/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.db

import com.kotlindiscord.kord.extensions.adapters.mongodb.MONGODB_URI
import com.kotlindiscord.kord.extensions.adapters.mongodb.kordExCodecRegistry
import com.mongodb.MongoException
import com.mongodb.kotlin.client.coroutine.MongoClient
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import mu.KotlinLogging
import org.bson.BsonInt64
import org.bson.Document

internal object Database {
	private val logger = KotlinLogging.logger {}
	private val client = MongoClient.create(MONGODB_URI)

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
			.withCodecRegistry(kordExCodecRegistry)
}
