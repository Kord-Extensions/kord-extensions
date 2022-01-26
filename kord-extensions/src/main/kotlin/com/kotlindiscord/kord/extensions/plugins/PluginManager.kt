/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins

import org.pf4j.DefaultPluginManager
import org.pf4j.PluginDescriptorFinder
import org.pf4j.PropertiesPluginDescriptorFinder
import java.nio.file.Path

/** Module manager, in charge of loading and managing module "plugins". **/
public open class PluginManager(roots: List<Path>) : DefaultPluginManager(roots) {
    override fun createPluginDescriptorFinder(): PluginDescriptorFinder =
        PropertiesPluginDescriptorFinder()
}
