/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.parser.tokens

/**
 * Data class representing a single positional argument token.
 *
 * @param data Argument data
 */
public data class PositionalArgumentToken(
	override val data: String,
) : Token<String>
