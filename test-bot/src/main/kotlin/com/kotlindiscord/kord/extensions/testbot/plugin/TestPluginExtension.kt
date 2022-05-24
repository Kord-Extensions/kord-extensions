/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.plugin

import com.kotlindiscord.kord.extensions.extensions.Extension

public class TestPluginExtension : Extension() {
    override val name: String = TestPlugin.PLUGIN_ID

    override suspend fun setup() {
        TODO("Not yet implemented")
    }
}
