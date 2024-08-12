/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.parser.tokens

/**
 * Simple base class for a parser token. Exists in order to make changes later easier.
 */
public interface Token<T : Any?> {
	/** Stored token data. **/
	public val data: T
}
