package com.kotlindiscord.kord.extensions.tooling

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
