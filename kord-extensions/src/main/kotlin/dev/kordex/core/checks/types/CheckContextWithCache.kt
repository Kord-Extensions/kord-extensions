/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.checks.types

import dev.kord.core.event.Event
import dev.kordex.core.utils.MutableStringKeyedMap
import java.util.*

/**
 * A check context that provides a cache map, specifically meant for use in event, component and command contexts
 * that provide a cache map for the lifecycle of the handler.
 *
 * @property cache Cache map, where you can store data you retrieve as part of your checks for use in later checks,
 * or your action block.
 */
public class CheckContextWithCache<out T : Event>(
	event: T,
	locale: Locale,

	public val cache: MutableStringKeyedMap<Any>,
) : CheckContext<T>(event, locale)
