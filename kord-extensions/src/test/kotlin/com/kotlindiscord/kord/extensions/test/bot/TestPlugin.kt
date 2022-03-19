/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.test.bot

import com.kotlindiscord.kord.extensions.plugins.KordExPlugin
import com.kotlindiscord.kord.extensions.plugins.annotations.plugins.WiredPlugin
import org.pf4j.PluginWrapper

@WiredPlugin(
    "test",
    "0.0.1"
)
class TestPlugin(wrapper: PluginWrapper) : KordExPlugin(wrapper) {
    override suspend fun setup() {
//        extension(::TestExtension)
    }
}
