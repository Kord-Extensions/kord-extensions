/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.pages

import kotlinx.serialization.Serializable

@Serializable
public abstract class Block {
	public abstract val classes: List<String>
	public abstract val id: String?
}
