/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
