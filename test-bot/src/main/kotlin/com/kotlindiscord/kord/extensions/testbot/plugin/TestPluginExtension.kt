/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.plugin

import com.kotlindiscord.kord.extensions.extensions.Extension
import com.kotlindiscord.kord.extensions.extensions.publicSlashCommand
import com.kotlindiscord.kord.extensions.plugins.PluginManager
import com.kotlindiscord.kord.extensions.plugins.extra.MappingsPlugin
import com.kotlindiscord.kord.extensions.testbot.utils.assert
import com.kotlindiscord.kord.extensions.types.respond
import org.koin.core.component.inject
import org.pf4j.PluginState

public class TestPluginExtension : Extension() {
    override val name: String = TestPlugin.PLUGIN_ID

    private val pluginManager: PluginManager by inject()

    override suspend fun setup() {
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

                assert(pluginIds.contains(MappingsPlugin.PLUGIN_ID)) {
                    "Test plugin (`${MappingsPlugin.PLUGIN_ID}`) should be loaded."
                }

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
                                    "`${desc.pluginClass.replace(
                                        "com.kotlindiscord.kord.extensions",
                                        "c.k.k.e"
                                    )}`"
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
