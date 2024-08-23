/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kordex.core.ExtensibleBot
import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.extensions.Extension
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/** Builder used to insert code at various points in the bot's lifecycle. **/
@Suppress("TooGenericExceptionCaught")
// We need to catch literally everything in here
@BotBuilderDSL
public class HooksBuilder {
	/**
	 * Whether Kord's shutdown hook should be registered. When enabled, Kord logs out of the gateway on shutdown.
	 */
	public var kordShutdownHook: Boolean = true

	// region: Hook lists

	/** @suppress Internal list of hooks. **/
	public val afterExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val afterKoinSetupList: MutableList<suspend () -> Unit> = mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val beforeKoinSetupList: MutableList<suspend () -> Unit> = mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val beforeExtensionsAddedList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val beforeStartList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val createdList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val extensionAddedList: MutableList<suspend ExtensibleBot.(extension: Extension) -> Unit> =
		mutableListOf()

	/** @suppress Internal list of hooks. **/
	public val setupList: MutableList<suspend ExtensibleBot.() -> Unit> = mutableListOf()

	// endregion

	// region DSL functions

	/**
	 * Register a lambda to be called after all the extensions in the [ExtensionsBuilder] have been added. This
	 * will be called regardless of how many were successfully set up.
	 */
	@BotBuilderDSL
	public fun afterExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
		afterExtensionsAddedList.add(body)

	/**
	 * Register a lambda to be called after Koin has been set up. You can use this to register overriding modules
	 * via `loadModule` before the modules are actually accessed.
	 */
	@BotBuilderDSL
	public fun afterKoinSetup(body: suspend () -> Unit): Boolean =
		afterKoinSetupList.add(body)

	/**
	 * Register a lambda to be called before Koin has been set up. You can use this to register Koin modules
	 * early, if needed.
	 */
	@BotBuilderDSL
	public fun beforeKoinSetup(body: suspend () -> Unit): Boolean =
		beforeKoinSetupList.add(body)

	/**
	 * Register a lambda to be called before all the extensions in the [ExtensionsBuilder] have been added.
	 */
	@BotBuilderDSL
	public fun beforeExtensionsAdded(body: suspend ExtensibleBot.() -> Unit): Boolean =
		beforeExtensionsAddedList.add(body)

	/**
	 * Register a lambda to be called just before the bot tries to connect to Discord.
	 */
	@BotBuilderDSL
	public fun beforeStart(body: suspend ExtensibleBot.() -> Unit): Boolean =
		beforeStartList.add(body)

	/**
	 * Register a lambda to be called right after the [ExtensibleBot] object has been created, before it gets set
	 * up.
	 */
	@BotBuilderDSL
	public fun created(body: suspend ExtensibleBot.() -> Unit): Boolean =
		createdList.add(body)

	/**
	 * Register a lambda to be called after any extension is successfully added to the bot.
	 */
	@BotBuilderDSL
	public fun extensionAdded(body: suspend ExtensibleBot.(extension: Extension) -> Unit): Boolean =
		extensionAddedList.add(body)

	/**
	 * Register a lambda to be called after the [ExtensibleBot] object has been created and set up.
	 */
	@BotBuilderDSL
	public fun setup(body: suspend ExtensibleBot.() -> Unit): Boolean =
		setupList.add(body)

	// endregion

	// region Hook execution functions

	/** @suppress Internal hook execution function. **/
	public suspend fun runAfterExtensionsAdded(bot: ExtensibleBot): Unit =
		afterExtensionsAddedList.forEach {
			try {
				it.invoke(bot)
			} catch (t: Throwable) {
				bot.logger.error(t) {
					"Failed to run extensionAdded hook $it"
				}
			}
		}

	/** @suppress Internal hook execution function. **/
	public suspend fun runAfterKoinSetup() {
		val logger: KLogger = KotlinLogging.logger {}

		afterKoinSetupList.forEach {
			try {
				it.invoke()
			} catch (t: Throwable) {
				logger.error(t) {
					"Failed to run afterKoinSetup hook $it"
				}
			}
		}
	}

	/** @suppress Internal hook execution function. **/
	public suspend fun runBeforeKoinSetup() {
		val logger: KLogger = KotlinLogging.logger {}

		beforeKoinSetupList.forEach {
			try {
				it.invoke()
			} catch (t: Throwable) {
				logger.error(t) {
					"Failed to run beforeKoinSetup hook $it"
				}
			}
		}
	}

	/** @suppress Internal hook execution function. **/
	public suspend fun runBeforeExtensionsAdded(bot: ExtensibleBot): Unit =
		beforeExtensionsAddedList.forEach {
			try {
				it.invoke(bot)
			} catch (t: Throwable) {
				bot.logger.error(t) {
					"Failed to run beforeExtensionsAdded hook $it"
				}
			}
		}

	/** @suppress Internal hook execution function. **/
	public suspend fun runBeforeStart(bot: ExtensibleBot): Unit =
		beforeStartList.forEach {
			try {
				it.invoke(bot)
			} catch (t: Throwable) {
				bot.logger.error(t) {
					"Failed to run beforeStart hook $it"
				}
			}
		}

	/** @suppress Internal hook execution function. **/
	public suspend fun runCreated(bot: ExtensibleBot): Unit =
		createdList.forEach {
			try {
				it.invoke(bot)
			} catch (t: Throwable) {
				bot.logger.error(t) {
					"Failed to run created hook $it"
				}
			}
		}

	/** @suppress Internal hook execution function. **/
	public suspend fun runExtensionAdded(bot: ExtensibleBot, extension: Extension): Unit =
		extensionAddedList.forEach {
			try {
				it.invoke(bot, extension)
			} catch (t: Throwable) {
				bot.logger.error(t) {
					"Failed to run extensionAdded hook $it"
				}
			}
		}

	/** @suppress Internal hook execution function. **/
	public suspend fun runSetup(bot: ExtensibleBot): Unit =
		setupList.forEach {
			try {
				it.invoke(bot)
			} catch (t: Throwable) {
				bot.logger.error(t) {
					"Failed to run setup hook $it"
				}
			}
		}

	// endregion
}
