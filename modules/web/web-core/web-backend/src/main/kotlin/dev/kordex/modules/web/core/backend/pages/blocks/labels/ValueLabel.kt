/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.pages.blocks.labels

import dev.kordex.modules.web.core.backend.pages.Block
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("value-label")
public class ValueLabel(
	public val value: String,
	public val template: String = "",  // TODO: Figure out template format
) : Block()
