/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.annotations

/** Marks a function that may result in unexpected behaviour, and ask the developer to check the docs. **/
@RequiresOptIn(
	message = "Calling this function may result in unexpected behaviour. Please ensure you read its documentation " +
		"comment before continuing.",
	level = RequiresOptIn.Level.WARNING
)
@Target(AnnotationTarget.FUNCTION)
public annotation class UnexpectedBehaviour
