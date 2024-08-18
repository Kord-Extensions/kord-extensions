/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.datacollection

import dev.kord.core.entity.Application
import dev.kord.gateway.Intents
import dev.kordex.core.*
import dev.kordex.core.annotations.InternalAPI
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.commands.application.ApplicationCommandRegistry
import dev.kordex.core.commands.application.DefaultApplicationCommandRegistry
import dev.kordex.core.commands.chat.ChatCommandRegistry
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.storage.StorageType
import dev.kordex.core.storage.StorageUnit
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
import java.io.IOException
import java.nio.file.Files
import java.util.*
import kotlin.time.Duration.Companion.minutes

@OptIn(InternalAPI::class)
@Suppress("StringLiteralDuplication", "MagicNumber")
public class DataCollector(public val level: DataCollection) : KordExKoinComponent {
	private lateinit var task: Task

	private val bot: ExtensibleBot by inject()
	private val settings: ExtensibleBotBuilder by inject()
	private val chatCommands: ChatCommandRegistry by inject()
	private val _applicationCommands: ApplicationCommandRegistry by inject()

	private val applicationCommands get() = _applicationCommands as? DefaultApplicationCommandRegistry

	private val logger = KotlinLogging.logger { }
	private val scheduler = Scheduler()
	private val systemInfo by lazy { SystemInfo() }

	private lateinit var applicationInfo: Application

	private val storageUnit = StorageUnit<State>(
		StorageType.Data,
		"kordex",
		"data-collection"
	)

	internal suspend fun migrate() {
		val props = loadOldState()

		if (props != null) {
			if (DATA_COLLECTION_UUID == null) {
				logger.info { "Migrating from '$COLLECTION_STATE_LOCATION' to storage units..." }

				setUUID(
					props.getProperty("uuid")?.let { UUID.fromString(it) }
				)

				deleteOldState()

				logger.info { "Migration complete!" }
			} else {
				logger.info {
					"Removing '$COLLECTION_STATE_LOCATION' as UUID configured via system property or " +
						"environmental variable."
				}

				deleteOldState()
			}
		}
	}

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

						chatCommandCount = if (settings.chatCommandsBuilder.enabled) {
							chatCommands.commands.size
						} else {
							0
						},

						messageCommandCount = applicationCommands
							?.let {
								it.messageCommands
									.filterValues { it.guildId == null }
									.size
							},

						slashCommandCount = applicationCommands
							?.let {
								it.slashCommands
									.filterValues { it.guildId == null }
									.size
							},

						userCommandCount = applicationCommands
							?.let {
								it.userCommands
									.filterValues { it.guildId == null }
									.size
							},

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

						chatCommandCount = if (settings.chatCommandsBuilder.enabled) {
							chatCommands.commands.size
						} else {
							0
						},

						messageCommandCount = applicationCommands
							?.let {
								it.messageCommands
									.filterValues { it.guildId == null }
									.size
							},

						slashCommandCount = applicationCommands
							?.let {
								it.slashCommands
									.filterValues { it.guildId == null }
									.size
							},

						userCommandCount = applicationCommands
							?.let {
								it.userCommands
									.filterValues { it.guildId == null }
									.size
							},

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
					if (lastUUID != null) {
						logger.debug { "Deleting collected data - last UUID: $lastUUID" }

						try {
							DataAPIClient.delete(lastUUID)

							setUUID(null)
						} catch (e: ResponseException) {
							logger.error {
								"Failed to remove collected data '$lastUUID' from the server: $e\n" +
									"\tThis will be re-attempted next time the bot starts. Please report this error " +
									"to Kord Extensions via the community links here: https://kordex.dev"
							}
						}
					}

					return stop()
				}
			}

			logger.debug { "Submitting collected data - level: ${level.readable}, last UUID: $lastUUID" }

			val response = DataAPIClient.submit(entity)

			setUUID(response)

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
		logger.info { "Starting data collector - level: ${level.readable}" }

		if (applicationCommands == null) {
			logger.info {
				"Application command registry doesn't extend `DefaultApplicationCommandRegistry`, not collecting " +
					"application command counts."
			}
		}

		migrate()

		val lastUUID = getUUID()

		if (level is DataCollection.None) {
			if (lastUUID != null) {
				try {
					logger.debug { "Deleting collected data - last UUID: $lastUUID" }

					DataAPIClient.delete(lastUUID)
				} catch (e: ResponseException) {
					logger.error {
						"Failed to remove collected data '$lastUUID' from the server: $e\n" +
							"\tThis will be re-attempted next time the bot starts. Please report this error to Kord " +
							"Extensions via the community links here: https://kordex.dev"
					}

					return stop()
				}

				setUUID(null)
			}

			return stop()
		}

		task = scheduler.schedule(
			callback = ::collect,
			delay = 30.minutes,
			name = "DataCollector",
			pollingSeconds = 10.minutes.inWholeSeconds,
			repeat = true,
			startNow = true
		)

		task.coroutineScope.launch {
			// So we don't have to wait for the first submission.
			task.callNow()
		}
	}

	internal suspend fun stop() {
		logger.info { "Shutting down data collector." }

		saveState()

		if (::task.isInitialized) {
			task.cancel()
		}
	}

	private fun deleteOldState() {
		val file = File(COLLECTION_STATE_LOCATION)

		try {
			Files.delete(file.toPath())
		} catch (e: IOException) {
			logger.warn(e) { "Failed to delete $COLLECTION_STATE_LOCATION" }
		}
	}

	private fun loadOldState(): Properties? {
		val props = Properties()

		val file = File(COLLECTION_STATE_LOCATION)

		if (file.exists()) {
			val reader = file.reader()

			try {
				props.load(reader)
			} finally {
				reader.close()
			}
		} else {
			return null
		}

		return props
	}

	private suspend fun getState(): State {
		if (DATA_COLLECTION_UUID != null) {
			// Don't save anything if pre-configured.

			return State(DATA_COLLECTION_UUID)
		}

		var current = storageUnit
			.withUser(bot.kordRef.selfId)
			.get()

		if (current == null) {
			current = State()

			storageUnit
				.withUser(bot.kordRef.selfId)
				.save(current)
		}

		return current
	}

	private suspend fun saveState() {
		if (DATA_COLLECTION_UUID != null) {
			// Don't save anything if pre-configured.

			return
		}

		storageUnit
			.withUser(bot.kordRef.selfId)
			.save()
	}

	/** Get the stored data collection UUID. **/
	public suspend fun getUUID(): UUID? =
		DATA_COLLECTION_UUID
			?: getState().uuid

	/** Get the stored data collection UUID. **/
	internal suspend fun setUUID(uuid: UUID?) {
		val state = getState()

		state.uuid = uuid

		saveState()
	}
}
