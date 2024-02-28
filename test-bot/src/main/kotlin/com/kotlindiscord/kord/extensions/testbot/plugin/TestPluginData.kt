/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.testbot.plugin

import com.kotlindiscord.kord.extensions.storage.Data
import kotlinx.serialization.Serializable
import net.peanuuutz.tomlkt.TomlComment

@Serializable
@Suppress("DataClassShouldBeImmutable")  // No.
public data class TestPluginData(
	@TomlComment("A test value. Nothing special here.")
	var key: String,
) : Data
