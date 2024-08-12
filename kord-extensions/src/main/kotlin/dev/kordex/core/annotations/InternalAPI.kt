/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations

@RequiresOptIn(
	level = RequiresOptIn.Level.ERROR,
	message = "This API is internal and should not be used, as it could be removed or changed without notice." +
		"An alternative API may be available - read the KDoc comment for this element to check. " +
		"In IntelliJ IDEA, you may jump to the definition by holding CTRL and clicking on the element."
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
public annotation class InternalAPI
