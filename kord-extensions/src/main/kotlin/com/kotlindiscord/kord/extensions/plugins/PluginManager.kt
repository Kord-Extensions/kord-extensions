/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins

import org.pf4j.*
import java.nio.file.Path

@Suppress("SpreadOperator")
/** Module manager, in charge of loading and managing module "plugins". **/
public open class PluginManager(roots: List<Path>) : JarPluginManager(*roots.toTypedArray()) {
	override fun createPluginDescriptorFinder(): PluginDescriptorFinder =
		PropertiesPluginDescriptorFinder()

	override fun createPluginLoader(): PluginLoader? {
		return CompoundPluginLoader()
			.add(DevelopmentLoader(this)) { this.isDevelopment }
			.add(JarLoader(this)) { this.isNotDevelopment }
			.add(DefaultLoader(this)) { this.isNotDevelopment }
	}
}
