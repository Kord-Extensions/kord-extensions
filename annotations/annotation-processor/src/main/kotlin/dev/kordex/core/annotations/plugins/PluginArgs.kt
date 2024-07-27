/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.annotations.plugins

import com.google.devtools.ksp.symbol.KSAnnotation
import dev.kordex.core.annotations.orNull

/**
 * Class representing the arguments that are defined within a wired plugin annotation, extracted from its declaration.
 *
 * @property annotation Annotation definition to extract data from.
 */
public data class PluginArgs(public val annotation: KSAnnotation) {
	/** @suppress **/
	private val argMap: Map<String?, Any?> =
		annotation.arguments
			.associate { it.name?.getShortName() to it.value }
			.filterKeys { it != null }

	/** @suppress **/
	public val id: String =
		argMap["id"] as String

	/** @suppress **/
	public val version: String =
		argMap["version"] as String

	/** @suppress **/
	public val author: String? =
		(argMap["author"] as String).orNull()

	/** @suppress **/
	public val description: String? =
		(argMap["description"] as String).orNull()

	/** @suppress **/
	public val license: String? =
		(argMap["license"] as String).orNull()

	/** @suppress **/
	public val kordExVersion: String? =
		(argMap["kordExVersion"] as String).orNull()

	/** @suppress **/
	@Suppress("UNCHECKED_CAST")
	public val dependencies: ArrayList<String> =
		argMap["dependencies"] as ArrayList<String>? ?: arrayListOf()
}
