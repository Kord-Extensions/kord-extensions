/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations

/** Marks a function that may result in unexpected behaviour, and ask the developer to check the docs. **/
@RequiresOptIn(
	message = "Calling or overriding this function may result in unexpected behaviour. Please ensure you read its " +
		"documentation comment before continuing.",
	level = RequiresOptIn.Level.WARNING
)
@Target(AnnotationTarget.FUNCTION)
public annotation class UnexpectedFunctionBehaviour
