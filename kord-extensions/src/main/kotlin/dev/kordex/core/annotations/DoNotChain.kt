/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations

/** Marks a class or function that's part of the bot builder DSLs. **/
@RequiresOptIn(
	message = "This function will cause an immediate REST call. If you want to do more than one operation here, " +
		"you should use `.edit { }` instead as that will result in a single REST call for all operations - instead " +
		"of a separate REST call for each operation.",
	level = RequiresOptIn.Level.WARNING
)
@Target(AnnotationTarget.FUNCTION)
public annotation class DoNotChain
