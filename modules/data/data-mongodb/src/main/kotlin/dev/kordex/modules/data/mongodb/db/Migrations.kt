/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.data.mongodb.db

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.github.oshai.kotlinlogging.KotlinLogging

private const val META_NAME: String = "data-adapter"

private val migrations = mutableMapOf<Int, suspend (db: MongoDatabase) -> Unit>(
	1 to { it.createCollection(Metadata.COLLECTION_NAME) },
)

internal object Migrations {
	private val logger = KotlinLogging.logger {}

	suspend fun migrate() {
		logger.info { "Running migrations..." }

		var adapterVersion = Metadata.get(META_NAME)
			?: 0

		val latestVersion = migrations.keys.max()

		while (adapterVersion < latestVersion) {
			adapterVersion += 1

			logger.debug { "Migrating: $META_NAME v${adapterVersion - 1} -> $adapterVersion" }

			migrations[adapterVersion]!!(Database.db)
			Metadata.set(META_NAME, adapterVersion)
		}

		logger.info { "Finished migrating database." }
	}
}
