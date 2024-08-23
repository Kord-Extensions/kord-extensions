/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.sentry.captures

import dev.kord.core.entity.Guild
import dev.kord.core.entity.Role
import dev.kord.core.entity.User
import dev.kord.core.entity.channel.Channel
import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.builders.extensions.SentryDataTypeBuilder
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.sentry.SentryAdapter
import dev.kordex.core.utils.MutableStringKeyedMap
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.koin.core.component.inject

/**
 * Base type representing a Sentry data capture.
 *
 * [SentryCapture] objects contain data to be sent to Sentry, allowing for containerised and modular data definitions.
 * They exist for the following reasons:
 *
 * * To provide a more centralised data model for Sentry data submissions.
 * * To make it possible to reliably configure what personal user data may be sent to Sentry.
 * * To provide an anchoring type that allows for the creation of various processing pipelines.
 */
@Suppress("StringLiteralDuplication")
public abstract class SentryCapture : KordExKoinComponent {
	/** Channel object to automatically populate data from. **/
	public open var channel: Channel? = null

	/** Guild object to automatically populate data from. **/
	public open var guild: Guild? = null

	/** User object to automatically populate data from. **/
	public open var user: User? = null

	/** Role object to automatically populate data from. **/
	public open var role: Role? = null

	protected val logger: KLogger = KotlinLogging.logger { }
	protected val sentry: SentryAdapter by inject()
	protected val settings: ExtensibleBotBuilder by inject()

	protected lateinit var allowedTypes: SentryDataTypeBuilder

	internal fun hasAllowedTypes() =
		this::allowedTypes.isInitialized

	internal fun setAllowedTypes(types: SentryDataTypeBuilder) {
		allowedTypes = types
	}

	/**
	 * Process the given map, returning a new map, prepared for submission to Sentry.
	 *
	 * This function processes the map in the following ways:
	 *
	 * * Omitting standardised keys that directly reference data disallowed by [allowedTypes].
	 * * Processing keys that end in `::type`, omitting them if [allowedTypes] disallows that type, and removing the
	 *   `::type` at the end of the key.
	 *   If [KEY_TYPES] does not contain the given type, the resulting map will contain the unmodified key.
	 *
	 * Before submission to Sentry, it is important to process all maps with this function.
	 */
	@Suppress("LoopWithTooManyJumpStatements")
	protected fun <T : Any> processMap(map: MutableStringKeyedMap<T>): MutableStringKeyedMap<T> {
		val processedData: MutableStringKeyedMap<T> = mutableMapOf()

		for ((key, value) in map) {
			if ("::" in key) {
				var (keyName, keyType) = key.split("::", limit = 2)

				keyType = keyType.lowercase()

				if (keyType.endsWith("s")) {
					keyType = keyType.trimEnd('s')
				}

				if (keyType !in KEY_TYPES) {
					logger.debug {
						"Not processing unknown map key type $keyType, " +
							"supported types are ${KEY_TYPES.joinToString()}. " +
							"Key $key will be used verbatim."
					}

					processedData[key] = value

					continue
				}

				if (keyType == "argument" && !allowedTypes.arguments) {
					logger.debug {
						"Filtering key $key, as argument data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (keyType == "channel" && !allowedTypes.channels) {
					logger.debug {
						"Filtering key $key, as channel data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (keyType == "guild" && !allowedTypes.guilds) {
					logger.debug {
						"Filtering key $key, as guild data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (keyType == "role" && !allowedTypes.roles) {
					logger.debug {
						"Filtering key $key, as role data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (keyType == "user" && !allowedTypes.users) {
					logger.debug {
						"Filtering key $key, as user data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				processedData[keyName] = value
			} else {
				val loweredKey = key.lowercase()

				if (
					!allowedTypes.arguments &&
					(
						loweredKey == "arguments" ||
							loweredKey == "argument" ||
							loweredKey.startsWith("arguments.") ||
							loweredKey.startsWith("argument.")
						)
				) {
					logger.debug {
						"Filtering key $key, as argument data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (
					!allowedTypes.channels &&
					(
						loweredKey == "channel" ||
							loweredKey == "channels" ||
							loweredKey.startsWith("channel.") ||
							loweredKey.startsWith("channels.")
						)
				) {
					logger.debug {
						"Filtering key $key, as channel data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (
					!allowedTypes.guilds &&
					(
						loweredKey == "guild" ||
							loweredKey == "guilds" ||
							loweredKey.startsWith("guild.") ||
							loweredKey.startsWith("guilds.")
						)
				) {
					logger.debug {
						"Filtering key $key, as guild data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (
					!allowedTypes.roles &&
					(
						loweredKey == "role" ||
							loweredKey == "roles" ||
							loweredKey.startsWith("role.") ||
							loweredKey.startsWith("roles.")
						)
				) {
					logger.debug {
						"Filtering key $key, as role data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				if (
					!allowedTypes.users &&
					(
						loweredKey == "user" ||
							loweredKey == "users" ||
							loweredKey.startsWith("user.") ||
							loweredKey.startsWith("users.")
						)
				) {
					logger.debug {
						"Filtering key $key, as user data is disabled in the SentryDataTypeBuilder."
					}

					continue
				}

				processedData[key] = value
			}
		}

		return processedData
	}

	public companion object {
		/** Array containing the supported special key types, used by [processMap] for user-defined keys. **/
		public val KEY_TYPES: Array<String> = arrayOf("argument", "channel", "guild", "role", "user")
	}
}
