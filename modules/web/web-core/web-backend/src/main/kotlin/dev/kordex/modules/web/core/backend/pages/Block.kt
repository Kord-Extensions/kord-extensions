/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.web.core.backend.pages

import kotlinx.serialization.Serializable

@Serializable
public abstract class Block {
	public open val classes: MutableList<String> = mutableListOf()
	public open val id: String? = null
	public open val content: String? = null
}
