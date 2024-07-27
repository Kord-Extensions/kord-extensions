/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.modules.web.core.backend.pages

import kotlinx.serialization.Serializable

@Serializable
public abstract class Block {
	public open val classes: MutableList<String> = mutableListOf()
	public open val id: String? = null
	public open val content: String? = null
}
