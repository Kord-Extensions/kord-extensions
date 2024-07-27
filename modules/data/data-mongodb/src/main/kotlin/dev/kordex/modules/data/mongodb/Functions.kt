/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.data.mongodb

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.modules.data.mongodb.db.Database

/**
 * Configures the bot to use MongoDB as the data storage adapter.
 *
 * This method sets up the [MongoDBDataAdapter] as the data adapter for the bot, allowing it to interact with a
 * MongoDB database for storing and retrieving data provided by storage units.
 *
 * Additionally, this method registers a hook with `beforeKoinSetup`, which checks that the database is reachable,
 * and runs any pending migrations.
 *
 * Usage:
 *
 * ```
 * ExtensibleBotBuilder(...) {
 *     mongoDB()
 * }
 * ```
 */
public suspend fun ExtensibleBotBuilder.mongoDB() {
	dataAdapter(::MongoDBDataAdapter)

	hooks {
		beforeKoinSetup {
			Database.setup()
		}
	}
}
