/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.events

import dev.kord.common.annotation.KordPreview
import dev.kord.core.Kord
import dev.kord.core.event.Event
import dev.kordex.core.koin.KordExKoinComponent
import dev.kordex.core.utils.MutableStringKeyedMap

/**
 * Base interface for events fired by Kord Extensions.
 */
public interface KordExEvent : Event, KordExKoinComponent {
	override val kord: Kord get() = getKoin().get()
	override val shard: Int get() = -1

	@KordPreview
	override val customContext: MutableStringKeyedMap<Any>
		get() = mutableMapOf()
}
