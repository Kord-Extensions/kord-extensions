/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core

import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.utils.envOrNull
import dev.kordex.data.api.DataCollection
import java.util.*

/** Convenient access to the properties stored within `kordex.properties` in your bot's resources. **/
public val kordexProps: Properties by lazy {
	val props = Properties()

	props.load(
		ExtensibleBotBuilder::class.java.getResourceAsStream(
			"/kordex.properties"
		)
	)

	props
}

/**
 * Location of the data collection state file.
 *
 * Don't delete this, otherwise KordEx can't automatically remove your data when you disable data collection.
 */
public val COLLECTION_STATE_LOCATION: String by lazy {
	System.getProperties()["dataCollectionState"] as? String
		?: envOrNull("DATA_COLLECTION_STATE")
		?: "./data/data-collection.properties"
}

/**
 * Data collection setting, defaulting to Standard if not set.
 *
 * Don't check this directly – use the `dataCollectionMode` property in `ExtensibleBotBuilder` instead!
 */
@InternalAPI
public val DATA_COLLECTION: DataCollection by lazy {
	val value = System.getProperties()["dataCollection"] as? String
		?: envOrNull("DATA_COLLECTION")
		?: kordexProps["settings.dataCollection"] as? String
		?: DataCollection.Standard.readable

	DataCollection.fromDB(value)
}

/**
 * Dev-mode configuration based on properties and env vars.
 *
 * Don't check this directly – use the `devMode` property in `ExtensibleBotBuilder` instead!
 */
@InternalAPI
public val DEV_MODE: Boolean by lazy {
	System.getProperties().contains("devMode") ||
		envOrNull("DEV_MODE") != null ||
		envOrNull("ENVIRONMENT") in arrayOf("dev", "development")
}

/** Configured first-party KordEx modules. **/
public val KORDEX_MODULES: List<String> by lazy {
	val modules = kordexProps["modules"] as? String

	modules?.split(", ")
		?: emptyList()
}

/** Current Kord Extensions version. **/
public val KORDEX_VERSION: String? by lazy {
	kordexProps["versions.kordEx"] as? String
}

/** Current Kord version. **/
public val KORD_VERSION: String? by lazy {
	kordexProps["versions.kord"] as? String
		?: kordexProps["kordVersion"] as? String
}
