/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.plugins

import dev.kord.core.Kord
import dev.kordex.core.ExtensibleBot
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.MutableStringKeyedMap
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.inject
import org.pf4j.Plugin

/**
 * Abstract class representing a plugin.
 *
 * The bot will always load registered plugins at the end of the setup process so that everything else is available
 * and ready to be used.
 */
public abstract class KordExPlugin : Plugin(), KordExKoinComponent {
	internal val extensions: MutableStringKeyedMap<ExtensionBuilder> = mutableMapOf()
	internal val settingsCallbacks: MutableList<SettingsCallback> = mutableListOf()

	/** Quick access to the bot object. **/
	public val bot: ExtensibleBot by inject()

	/** Quick access to the Kord object. **/
	public val kord: Kord by inject()

	/**
	 * Override this to set up and configure your plugin.
	 *
	 * You'll want to call [extension] to register any extensions here, so they can be loaded and unloaded with
	 * the plugin.
	 */
	public abstract suspend fun setup()

	/**
	 * Register an extension to be loaded that's part of this plugin.
	 *
	 * You'll probably just want to pass the constructor for your extension. However, **do note that the builder
	 * will be called immediately,** in order to resolve its name. It will be called again later when the bot
	 * loads up your extensions.
	 */
	public open fun extension(builder: ExtensionBuilder) {
		val extension = builder()

		extensions[extension.name] = builder
	}

	/**
	 * Modify the current bot's settings.
	 *
	 * **Do not register extensions here, use [extension]!**
	 */
	public open fun settings(body: SettingsCallback) {
		settingsCallbacks.add(body)
	}

	internal suspend fun asyncStart() {
		setup()

		extensions.values.forEach { bot.addExtension(it) }
	}

	internal suspend fun asyncStop() {
		extensions.keys.forEach {
			bot.unloadExtension(it)
		}
	}

	internal suspend fun asyncDelete() {
		extensions.keys.forEach {
			bot.removeExtension(it)
		}
	}

	override fun start(): Unit = runBlocking {
		kord.launch { asyncStart() }.join()
	}

	override fun stop(): Unit = runBlocking {
		kord.launch { asyncStop() }.join()
	}

	override fun delete(): Unit = runBlocking {
		kord.launch { asyncDelete() }.join()
	}
}
