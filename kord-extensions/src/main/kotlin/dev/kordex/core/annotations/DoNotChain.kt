/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
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
