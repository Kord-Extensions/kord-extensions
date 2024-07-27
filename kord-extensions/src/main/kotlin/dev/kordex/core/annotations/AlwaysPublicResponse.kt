/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.annotations

/** Marks a function that always results in public interaction responses. **/
@RequiresOptIn(
	message = "This function will always result in a public interaction response, even if used within an " +
		"ephemeral interaction.",
	level = RequiresOptIn.Level.WARNING
)
@Target(AnnotationTarget.FUNCTION)
public annotation class AlwaysPublicResponse
