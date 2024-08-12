/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.annotations.tooling

/**
 * Tooling annotation representing something that relates to the translation system.
 *
 * @param type How the annotated element relates to the translation system.
 */
@MustBeDocumented
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.VALUE_PARAMETER)
public annotation class Translatable(
	val type: TranslatableType,
)
