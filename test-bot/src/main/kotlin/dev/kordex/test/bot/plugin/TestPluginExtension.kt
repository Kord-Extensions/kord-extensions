/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.test.bot.plugin

import dev.kordex.core.commands.Arguments
import dev.kordex.core.commands.application.slash.publicSubCommand
import dev.kordex.core.commands.converters.impl.string
import dev.kordex.core.extensions.Extension
import dev.kordex.core.extensions.publicSlashCommand
import dev.kordex.core.plugins.PluginManager
import org.koin.core.component.inject
import org.pf4j.PluginState

public class TestPluginExtension : Extension() {
	override val name: String = TestPlugin.PLUGIN_ID

	private val pluginManager: PluginManager by inject()

	public inner class ConfigSetArgs : Arguments() {
		public val value: String by string {
			name = "value"
			description = "Value to set. Can be anything."
		}
	}

	override suspend fun setup() {
		publicSlashCommand {
			name = "plugin-config"
			description = "Commands for working with the test plugin's configuration."

			publicSubCommand {
				name = "delete"
				description = "Delete  current set configuration value."

				action {
					TestPlugin.DATA_UNIT
						.withUser(event.interaction.user.id)
						.delete()

					respond {
						content = "User value deleted."
					}
				}
			}

			publicSubCommand {
				name = "get"
				description = "Get the current set configuration value."

				action {
					val value = TestPlugin.DATA_UNIT
						.withUser(event.interaction.user.id)
						.get()
						?.key

					respond {
						content = if (value == null) {
							"No user value has been set."
						} else {
							"**User value:** `$value`"
						}
					}
				}
			}

			publicSubCommand(::ConfigSetArgs) {
				name = "set"
				description = "Set a new configuration value."

				action {
					val dataUnit = TestPlugin.DATA_UNIT
						.withUserFrom(event)

					val value = dataUnit.get()
						?: TestPluginData(key = arguments.value)

					value.key = arguments.value

					dataUnit.save(value)

					respond {
						content = "**User value set:** `${value.key}`"
					}
				}
			}
		}

		publicSlashCommand {
			name = "plugins"
			description = "Retrieve the list of loaded plugins."

			action {
				val loadedPlugins = pluginManager.plugins
					.filter { it.pluginState == PluginState.STARTED }
					.sortedBy { it.descriptor.pluginId }

				val pluginIds = loadedPlugins.map { it.descriptor.pluginId }

				assert(pluginIds.contains(TestPlugin.PLUGIN_ID)) {
					"Test plugin (`${TestPlugin.PLUGIN_ID}`) should be loaded."
				}

// 				assert(pluginIds.contains(MappingsPlugin.PLUGIN_ID)) {
// 					"Test plugin (`${MappingsPlugin.PLUGIN_ID}`) should be loaded."
// 				}

				respond {
					content = buildString {
						appendLine("**${loadedPlugins.size}** loaded plugins.")
						appendLine()

						loadedPlugins.forEach { plugin ->
							val desc = plugin.descriptor

							appendLine("**»** `${desc.pluginId}@${desc.version}`")
							appendLine()

							appendLine(
								"**Class:** " +
									"`${
										desc.pluginClass.replace(
											"dev.kordex.core",
											"d.k.c"
										)
									}`"
							)

							appendLine(
								"**Dependencies:** `${
									desc.dependencies.joinToString { "`${it.pluginId}`" }.ifEmpty { "None" }
								}`"
							)

							appendLine("**License:** `${desc.license}`")
							appendLine("**Provider:** `${desc.provider}`")
							appendLine("**Requires:** `${desc.requires.ifEmpty { "N/A" }}`")
							appendLine()

							desc.pluginDescription.lines().forEach { line ->
								appendLine("> $line")
							}

							appendLine()
						}
					}
				}
			}
		}

		publicSlashCommand {
			name = "fail-assertion"
			description = "Intentionally fail an assertion."

			action {
				assert(false) {
					"**Assertion failed:** Intentional assertion failure."
				}
			}
		}
	}
}
