/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations

@RequiresOptIn(
	level = RequiresOptIn.Level.WARNING,
	message = "A String or Strings provided here will not be translated. " +
		"If you intended to translate, please switch to using Key objects."
)
@Target(
	AnnotationTarget.CLASS,
	AnnotationTarget.TYPEALIAS,
	AnnotationTarget.FUNCTION,
	AnnotationTarget.PROPERTY,
	AnnotationTarget.FIELD,
	AnnotationTarget.CONSTRUCTOR,
	AnnotationTarget.PROPERTY_SETTER,
	AnnotationTarget.PROPERTY_SETTER
)
public annotation class NotTranslated
