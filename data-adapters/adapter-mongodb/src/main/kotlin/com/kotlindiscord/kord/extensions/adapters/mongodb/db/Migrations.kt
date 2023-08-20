/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.db

import com.mongodb.kotlin.client.coroutine.MongoDatabase
import mu.KotlinLogging

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
