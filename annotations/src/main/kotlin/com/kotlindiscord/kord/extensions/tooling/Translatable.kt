/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

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
