/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.tooling

/**
 * Enum explaining how an annotated type relates to the translation system.
 */
public enum class TranslatableType {
	/** The annotated element specifies a bundle name. **/
	BUNDLE,

	/** The annotated element specifies a locale. **/
	LOCALE,

	/** The annotated element specifies an optionally translated string. **/
	STRING,
}
