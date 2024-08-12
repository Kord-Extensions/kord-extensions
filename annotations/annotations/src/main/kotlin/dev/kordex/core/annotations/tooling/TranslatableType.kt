/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations.tooling

/**
 * Enum explaining how an annotated type relates to the translation system.
 */
public enum class TranslatableType {
	/** The annotated element specifies a bundle name. **/
	BUNDLE,

	/** The annotated element specifies a locale. **/
	LOCALE,

	/** The annotated element specifies an optionally translated string. **/
	STRING,
}
