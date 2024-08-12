/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations.converters

/**
 * Enum representing different types of converter functions.
 *
 * @property fragment String fragment to add to the converter type name
 * @property order Order of types - for naming order and compatibility
 * @property appendFragment Whether to append the string fragment, or ignore it
 */
public enum class ConverterType(
	public val fragment: String,
	public val order: Int,
	public val appendFragment: Boolean = true,
) {
	DEFAULTING("Defaulting", 0),
	LIST("List", 0),
	OPTIONAL("Optional", 0),

	COALESCING("Coalescing", 1),
	SINGLE("", 1),

	CHOICE("Choice", 2, false);

	public companion object {
		/** Given the `.name` or `.simpleName` of a converter type, get the relevant enum entry. **/
		public fun fromName(name: String): ConverterType? =
			when (name) {
				DEFAULTING.name -> DEFAULTING
				LIST.name -> LIST
				OPTIONAL.name -> OPTIONAL

				COALESCING.name -> COALESCING
				SINGLE.name -> SINGLE

				CHOICE.name -> CHOICE

				else -> null
			}
	}
}
