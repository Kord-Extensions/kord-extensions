/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.plugins

import dev.kordex.core.builders.ExtensibleBotBuilder
import dev.kordex.core.extensions.Extension

/** Type alias representing a callable that operates against a bot's settings builder. **/
public typealias SettingsCallback = suspend (ExtensibleBotBuilder).() -> Unit

/** Type alias representing an extension builder, likely just a constructor. **/
public typealias ExtensionBuilder = () -> Extension
