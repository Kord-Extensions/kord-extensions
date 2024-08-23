/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.plugins.PluginManager
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.div

/**
 * Builder used for configuring the bot's wired-plugin-loading options.
 *
 * @property parent Parent [ExtensibleBotBuilder], for extension functions.
 */
@BotBuilderDSL
public class PluginBuilder(public val parent: ExtensibleBotBuilder) {
	internal lateinit var managerObj: PluginManager

	/** Whether to attempt to load wired plugin. Defaults to `true`. **/
	public var enabled: Boolean = true

	/** Plugin manager builder, which you can replace if your needs require it. **/
	public var manager: (List<Path>) -> PluginManager = ::PluginManager

	/** List of paths to load plugin from. Uses `plugins/` in the current working directory by default. **/
	public val pluginPaths: MutableList<Path> = mutableListOf(
		Path(".") / "plugins"
	)

	/** List of plugin IDs to disable. Plugins in this list will not be loaded automatically. **/
	public val disabledPlugins: MutableList<String> = mutableListOf()

	/**
	 * Convenience function for disabling a plugin by ID.
	 *
	 * @see disabledPlugins
	 */
	public fun disable(id: String) {
		disabledPlugins.add(id)
	}

	/**
	 * Convenience function for adding a plugin path.
	 *
	 * @see pluginPaths
	 */
	public fun pluginPath(path: String) {
		pluginPaths.add(Path.of(path))
	}

	/**
	 * Convenience function for adding a plugin path.
	 *
	 * @see pluginPaths
	 */
	public fun pluginPath(path: Path) {
		pluginPaths.add(path)
	}
}
