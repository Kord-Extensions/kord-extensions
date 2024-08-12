/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
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
