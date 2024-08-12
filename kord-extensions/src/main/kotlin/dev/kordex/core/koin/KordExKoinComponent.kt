/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.koin

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

/**
 * [KoinComponent] that gives access to dependencies from Koin app within [KordExContext].
 */
public interface KordExKoinComponent : KoinComponent {
	/**
	 * Get the associated Koin instance.
	 *
	 * @throws IllegalStateException KoinApplication not yet started.
	 */
	override fun getKoin(): Koin = KordExContext.get()
}
