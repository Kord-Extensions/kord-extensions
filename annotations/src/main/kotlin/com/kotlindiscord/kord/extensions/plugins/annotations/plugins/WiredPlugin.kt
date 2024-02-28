/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.annotations.plugins

/**
 * Annotation used to denote an automatically-loaded (wired) KordEx plugin.
 *
 * * [Semantic Expressions](https://github.com/zafarkhaja/jsemver#semver-expressions-api-ranges)
 *
 * @property id Plugin ID, must be unique.
 * @property version Plugin version, must be semver.
 *
 * @property author Optional, the author to credit for providing this plugin.
 * @property description Optional, information about this plugin.
 * @property license Optional, license used for this plugin.
 * @property kordExVersion Optional, Semantic Expression describing the required versions of Kord Extensions.
 *
 * @property dependencies Optional, array of other wired plugin IDs this one depends upon. Versions may be specified
 * by suffixing the plugin ID with an `@` and a Semantic Expression. Dependencies may be optional by suffixing the ID
 * with a question mark (`?`), placed before the `@` if a version requirement is specified.
 */
@Target(AnnotationTarget.CLASS)
@MustBeDocumented
public annotation class WiredPlugin(
	public val id: String,
	public val version: String,

	public val author: String = "",
	public val description: String = "",
	public val license: String = "",
	public val kordExVersion: String = "",

	public val dependencies: Array<String> = [],
)
