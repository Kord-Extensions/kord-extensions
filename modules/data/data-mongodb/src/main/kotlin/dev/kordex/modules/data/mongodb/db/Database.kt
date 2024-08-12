/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
