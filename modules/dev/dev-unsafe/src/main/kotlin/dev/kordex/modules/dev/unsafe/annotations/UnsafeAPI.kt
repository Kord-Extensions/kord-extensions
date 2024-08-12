/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.dev.unsafe.annotations

/** Annotation used to mark unsafe APIs. Should be applied to basically everything in this module. **/
@RequiresOptIn(
	message = "This API is unsafe, and is only intended for advanced use-cases. If you're not entirely sure that " +
		"you need to use this, you should look for a safer API that's provided by a different Kord Extensions module."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.FIELD, AnnotationTarget.TYPEALIAS)
public annotation class UnsafeAPI
