/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.constraints

import com.kotlindiscord.kord.extensions.plugins.PluginManager
import com.kotlindiscord.kord.extensions.plugins.types.PluginManifest
import io.github.z4kn4fein.semver.constraints.satisfiedBy

private const val MAX_DEPTH: Int = 5

public open class ConstraintChecker(
	protected val pluginManager: PluginManager<*>,
) {
	public open fun checkAll(manifests: Map<String, PluginManifest>): MutableMap<String, List<ConstraintResult>> {
		val results: MutableMap<String, List<ConstraintResult>> = mutableMapOf()

		manifests.forEach { (pluginId, _) ->
			val result = checkPlugin(pluginId, manifests)

			if (result.isNotEmpty()) {
				results[pluginId] = result
			}
		}

		return results
	}

	public open fun checkPlugin(
		pluginId: String,
		manifests: Map<String, PluginManifest>,
		depth: Int = 0,
		alreadyExamined: MutableSet<String> = mutableSetOf(),
	): List<ConstraintResult> {
		val results: MutableList<ConstraintResult> = mutableListOf()

		val manifest = manifests[pluginId]
			?: return emptyList()  // Likely from the "extraVersions" map in the plugin manager

		alreadyExamined.add(pluginId)

		if (depth >= MAX_DEPTH) {
			error("Maximum recursion depth exceeded")
		}

		for ((conflictingId, constraint) in manifest.constraints.conflicts) {
			val conflictingVersion = manifests[conflictingId]?.version
				?: pluginManager.extraVersions[conflictingId]
				?: continue

			if (constraint satisfiedBy conflictingVersion) {
				results.add(
					ConstraintResult.Conflict(
						pluginId,
						conflictingId,
						conflictingVersion,
						constraint
					)
				)
			}
		}

		for ((neededId, constraint) in manifest.constraints.needs) {
			val neededVersion = manifests[neededId]?.version
				?: pluginManager.extraVersions[neededId]

			if (neededVersion == null) {
				results.add(
					ConstraintResult.Missing(
						pluginId,
						neededId,
						constraint
					)
				)
			} else if (!(constraint satisfiedBy neededVersion)) {
				results.add(
					ConstraintResult.WrongNeededVersion(
						pluginId,
						neededId,
						neededVersion,
						constraint
					)
				)
			} else {
				if (neededId in alreadyExamined) {
					continue
				}

				val innerResults = checkPlugin(neededId, manifests, depth + 1, alreadyExamined)

				if (innerResults.isNotEmpty()) {
					results.add(
						ConstraintResult.NeededPluginFailedConstraints(
							pluginId, neededId, innerResults
						)
					)
				}
			}
		}

		for ((wantedId, constraint) in manifest.constraints.wants) {
			val wantedVersion = manifests[wantedId]?.version
				?: pluginManager.extraVersions[wantedId]
				?: continue

			if (!(constraint satisfiedBy wantedVersion)) {
				results.add(
					ConstraintResult.WrongWantedVersion(
						pluginId,
						wantedId,
						wantedVersion,
						constraint
					)
				)
			} else {
				if (wantedId in alreadyExamined) {
					continue
				}

				val innerResults = checkPlugin(wantedId, manifests, depth + 1, alreadyExamined)

				if (innerResults.isNotEmpty()) {
					results.add(
						ConstraintResult.WantedPluginFailedConstraints(
							pluginId, wantedId, innerResults
						)
					)
				}
			}
		}

		if (results.isNotEmpty()) {
			manifests[pluginId]?.failed = true
		}

		return results
	}
}
