/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.plugins

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.extensions.Extension

/** Type alias representing a callable that operates against a bot's settings builder. **/
public typealias SettingsCallback = suspend (ExtensibleBotBuilder).() -> Unit

/** Type alias representing an extension builder, likely just a constructor. **/
public typealias ExtensionBuilder = () -> Extension
