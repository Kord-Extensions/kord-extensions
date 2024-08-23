/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kordex.core.annotations.BotBuilderDSL
import dev.kordex.core.components.ComponentRegistry

/** Builder used to configure the bot's components settings. **/
@BotBuilderDSL
public class ComponentsBuilder {

	/** @suppress Component registry builder. **/
	public var registryBuilder: () -> ComponentRegistry = ::ComponentRegistry

	/**
	 * Register a builder (usually a constructor) returning a [ComponentRegistry] instance, which may be useful
	 * if you need to register a custom subclass.
	 */
	public fun registry(builder: () -> ComponentRegistry) {
		registryBuilder = builder
	}
}
