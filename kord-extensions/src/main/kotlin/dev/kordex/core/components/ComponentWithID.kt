/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.components

import java.util.*

/** Abstract class representing a component with an ID, which defaults to a newly-generated UUID. **/
public abstract class ComponentWithID : Component() {
	/** Component's ID, a UUID by default. **/
	public open var id: String = UUID.randomUUID().toString()

	public override fun validate() {
		if (id.isEmpty()) {
			error("All components must have a unique ID.")
		}
	}
}
