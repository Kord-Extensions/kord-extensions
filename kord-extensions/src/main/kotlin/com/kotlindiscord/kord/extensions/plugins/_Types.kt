/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins

import com.kotlindiscord.kord.extensions.builders.ExtensibleBotBuilder
import com.kotlindiscord.kord.extensions.extensions.Extension

/** Type alias representing a callable that operates against a bot's settings builder. **/
public typealias SettingsCallback = suspend (ExtensibleBotBuilder).() -> Unit

/** Type alias representing an extension builder, likely just a constructor. **/
public typealias ExtensionBuilder = () -> Extension
