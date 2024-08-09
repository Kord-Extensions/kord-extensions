/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.datacollection

import dev.kord.core.entity.Application
import dev.kord.gateway.Intents
import dev.kordex.core.*
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.getName
import dev.kordex.core.utils.scheduling.Scheduler
import dev.kordex.core.utils.scheduling.Task
import dev.kordex.data.api.DataCollection
import dev.kordex.data.api.types.Entity
import dev.kordex.data.api.types.impl.ExtraDataEntity
import dev.kordex.data.api.types.impl.MinimalDataEntity
import dev.kordex.data.api.types.impl.StandardDataEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.plugins.*
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import org.koin.ext.getFullName
import oshi.SystemInfo
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(InternalAPI::class)
@Suppress("StringLiteralDuplication", "MagicNumber")
public class DataCollector(public val level: DataCollection) : KordExKoinComponent {
	private lateinit var task: Task

	private val bot: ExtensibleBot by inject()
	private val settings: ExtensibleBotBuilder by inject()

	private val logger = KotlinLogging.logger { }
	private val scheduler = Scheduler()
	private val state: Properties = loadState()
	private val systemInfo by lazy { SystemInfo() }

	private lateinit var applicationInfo: Application

	@OptIn(InternalAPI::class)
	@Suppress("TooGenericExceptionCaught")
	internal suspend fun collect() {
		if (!::applicationInfo.isInitialized) {
			applicationInfo = bot.kordRef.getApplicationInfo()
		}

		try {
			lateinit var entity: Entity
			val lastUUID = getUUID()

			when (level) {
				is DataCollection.Minimal ->
					entity = MinimalDataEntity(
						id = lastUUID,

						devMode = settings.devMode,
						kordExVersion = KORDEX_VERSION ?: "Unknown",
						kordVersion = KORD_VERSION ?: "Unknown",

						modules = KORDEX_MODULES.associateWith {
							KORDEX_VERSION ?: "Unknown"
						},
					)

				is DataCollection.Standard ->
					entity = StandardDataEntity(
						id = lastUUID,

						devMode = settings.devMode,
						kordExVersion = KORDEX_VERSION ?: "Unknown",
						kordVersion = KORD_VERSION ?: "Unknown",

						modules = KORDEX_MODULES.associateWith {
							KORDEX_VERSION ?: "Unknown"
						},

						botId = bot.kordRef.selfId.toString(),
						botName = applicationInfo.name,
						extensionCount = bot.extensions.size,
						guildCount = bot.kordRef.guilds.count(),

						intents = settings.intentsBuilder
							?.let { Intents(it).values.map { i -> i.getName() }.toTypedArray() }
							?: arrayOf(),

						pluginCount = settings.pluginBuilder.managerObj.plugins.size,
						jvmVersion = System.getProperty("java.version"),
						kotlinVersion = KotlinVersion.CURRENT.toString(),
					)

				is DataCollection.Extra -> {
					val hardware = systemInfo.hardware
					val processor = hardware.processor

					entity = ExtraDataEntity(
						id = lastUUID,

						devMode = settings.devMode,
						kordExVersion = KORDEX_VERSION ?: "Unknown",
						kordVersion = KORD_VERSION ?: "Unknown",

						modules = KORDEX_MODULES.associateWith {
							KORDEX_VERSION ?: "Unknown"
						},

						botId = bot.kordRef.selfId.toString(),
						botName = applicationInfo.name,
						extensionCount = bot.extensions.size,
						guildCount = bot.kordRef.guilds.count(),

						intents = settings.intentsBuilder
							?.let { Intents(it).values.map { i -> i.getName() }.toTypedArray() }
							?: arrayOf(),

						pluginCount = settings.pluginBuilder.managerObj.plugins.size,

						cpuCount = processor.physicalProcessorCount,
						cpuGhz = processor.maxFreq.toFloat().div(1_000_000_000F),  // GHz, not Hz

						eventHandlerTypes = bot.eventHandlers
							.map { it.type.getFullName() }
							.toTypedArray(),

						extensions = bot.extensions.keys.toTypedArray(),
						jvmVersion = System.getProperty("java.version"),
						kotlinVersion = KotlinVersion.CURRENT.toString(),
						plugins = settings.pluginBuilder.managerObj.plugins.map { it.descriptor.pluginId }
							.toTypedArray(),
						ramAvailable = hardware.memory.total,
						threadCount = processor.logicalProcessorCount
					)

					if (applicationInfo.team != null) {
						entity = entity.copy(
							teamId = applicationInfo.teamId.toString(),
							teamName = applicationInfo.team?.name
						)
					}
				}

				else -> {
					val uuid = getUUID()

					if (uuid != null) {
						DataAPIClient.delete(uuid)
					}

					return stop()
				}
			}

			logger.debug { "Submitting collected data - level: ${level.readable}, last UUID: $lastUUID" }

			val response = DataAPIClient.submit(entity)

			setUUID(response)
			setLastLevel(level)

			saveState()

			if (lastUUID == response) {
				logger.debug { "Updated collected data successfully - UUID: $response" }
			} else {
				logger.debug { "Submitted collected data successfully - UUID: $response" }
			}
		} catch (e: Exception) {
			if (settings.devMode) {
				logger.warn { "Failed to submit collected data: $e" }
			} else {
				logger.warn(e) { "Failed to submit collected data" }
			}
		}
	}

	internal suspend fun start() {
		logger.info { "Staring data collector - level: ${level.readable}" }

		val lastLevel = getLastLevel()
		val lastUUID = getUUID()

		if (lastLevel !is DataCollection.None && level is DataCollection.None) {
			if (lastUUID != null) {
				try {
					DataAPIClient.delete(lastUUID)
				} catch (e: ResponseException) {
					logger.error {
						"Failed to remove collected data '$lastUUID' from the server: $e\n" +
							"\tThis will be re-attempted next time the bot starts. Please report this error to Kord " +
							"Extensions via the community links here: https://kordex.dev"
					}

					return stop()
				}

				logger.info { "Collected data '$lastUUID' has been deleted from the server." }
			}

			setUUID(null)
			setLastLevel(level)

			return stop()
		}

		setLastLevel(level)
		saveState()

		task = scheduler.schedule(
			callback = ::collect,
			delay = 30.minutes,
			name = "DataCollector",
			pollingSeconds = 600.seconds.inWholeSeconds,
			repeat = true,
			startNow = true
		)

		task.coroutineScope.launch {
			// So we don't have to wait for the first submission
			task.callNow()
		}
	}

	internal fun stop() {
		logger.info { "Shutting down data collector." }

		saveState()

		if (::task.isInitialized) {
			task.cancel()
		}
	}

	private fun loadState(): Properties {
		val props = Properties()

		val file = File(COLLECTION_STATE_LOCATION)

		if (!file.parentFile.exists()) {
			file.parentFile.mkdirs()
		}

		if (file.exists()) {
			props.load(file.reader())
		} else {
			props.setProperty("lastLevel", level.readable)
			props.store(file.writer(), "KordEx data collection state. Do not delete!")
		}

		return props
	}

	private fun saveState() {
		val file = File(COLLECTION_STATE_LOCATION)

		if (!file.parentFile.exists()) {
			file.parentFile.mkdirs()
		}

		state.store(file.writer(), "KordEx data collection state. Do not delete!")
	}

	/** Get the stored "last" data collection level. **/
	public fun getLastLevel(): DataCollection? {
		val prop = state.getProperty("lastLevel")

		return if (prop != null) {
			DataCollection.fromDB(prop)
		} else {
			null
		}
	}

	private fun setLastLevel(value: DataCollection?) {
		if (value != null) {
			state.setProperty("lastLevel", value.readable)
		} else {
			state.remove("lastLevel")
		}

		saveState()
	}

	/** Get the stored data collection UUID. **/
	public fun getUUID(): UUID? {
		val prop = state.getProperty("uuid")

		return if (prop != null) {
			UUID.fromString(prop)
		} else {
			null
		}
	}

	private fun setUUID(value: UUID?) {
		if (value != null) {
			state.setProperty("uuid", value.toString())
		} else {
			state.remove("uuid")
		}

		saveState()
	}
}
