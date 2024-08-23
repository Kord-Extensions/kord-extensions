/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.builders

import dev.kord.cache.api.DataCache
import dev.kord.core.ClientResources
import dev.kord.core.Kord
import dev.kord.core.cache.KordCacheBuilder
import dev.kord.core.cache.lruCache
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.EntitySupplyStrategy
import dev.kordex.core.annotations.BotBuilderDSL

/** Builder used for configuring the bot's caching options. **/
@BotBuilderDSL
public class CacheBuilder {
	/**
	 * Number of messages to keep in the cache. Defaults to 10,000.
	 *
	 * To disable automatic configuration of the message cache, set this to `null` or `0`. You can configure the
	 * cache yourself using the [kord] function, and interact with the resulting [DataCache] object using the
	 * [transformCache] function.
	 */
	@Suppress("MagicNumber")
	public var cachedMessages: Int? = 10_000

	/** The default Kord caching strategy - defaults to caching REST when an entity doesn't exist in the cache. **/
	public var defaultStrategy: EntitySupplyStrategy<EntitySupplier> =
		EntitySupplyStrategy.cacheWithCachingRestFallback

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var builder: (KordCacheBuilder.(resources: ClientResources) -> Unit) = {
		if (cachedMessages != null && cachedMessages!! > 0) {
			messages(lruCache(cachedMessages!!))
		}
	}

	/** @suppress Builder that shouldn't be set directly by the user. **/
	public var dataCacheBuilder: suspend Kord.(cache: DataCache) -> Unit = {}

	/** DSL function allowing you to customize Kord's cache. **/
	public fun kord(builder: KordCacheBuilder.(resources: ClientResources) -> Unit) {
		this.builder = {
			if (cachedMessages != null && cachedMessages!! > 0) {
				messages(lruCache(cachedMessages!!))
			}

			builder.invoke(this, it)
		}
	}

	/** DSL function allowing you to interact with Kord's [DataCache] before it connects to Discord. **/
	public fun transformCache(builder: suspend Kord.(cache: DataCache) -> Unit) {
		this.dataCacheBuilder = builder
	}
}
