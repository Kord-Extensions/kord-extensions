/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.extra.web.pages.blocks.labels

import dev.kordex.extra.web.pages.Block
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("text-label")
public class TextLabel(
	public val text: String,

	override val classes: List<String> = emptyList(),
	override val id: String? = null
) : Block()
