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
@SerialName("value-label")
public class ValueLabel(
	public val value: String,
	public val template: String = "",  // TODO: Figure out template format

	override val classes: List<String> = emptyList(),
	override val id: String? = null
) : Block()
