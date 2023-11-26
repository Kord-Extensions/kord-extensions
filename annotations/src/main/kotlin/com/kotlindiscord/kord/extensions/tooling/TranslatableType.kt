package com.kotlindiscord.kord.extensions.tooling

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
