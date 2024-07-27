/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
