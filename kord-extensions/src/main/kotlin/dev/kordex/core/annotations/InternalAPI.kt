/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
