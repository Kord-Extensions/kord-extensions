/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.plugins.constraints

import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.constraints.Constraint

private const val INDENT = "\t -> "

public sealed class ConstraintResult(
	public val pluginId: String,
	public open val readableString: String,
) {
	public class Conflict(
		pluginId: String,

		public val conflictingPluginId: String,
		public val conflictingPluginVersion: Version,
		public val constraint: Constraint,
	) : ConstraintResult(
		pluginId,
		"Conflicts with \"$conflictingPluginId\" version $conflictingPluginVersion " +
			"(constraint: $constraint)"
	) {
		override fun toString(): String =
			"Conflict: $pluginId -> $conflictingPluginId v$conflictingPluginVersion ($constraint)"
	}

	public class Missing(
		pluginId: String,

		public val neededPluginId: String,
		public val constraint: Constraint,
	) : ConstraintResult(
		pluginId,
		"Needs \"$neededPluginId\" (constraint: $constraint), but it's missing"
	) {
		override fun toString(): String =
			"Missing: $pluginId -> $neededPluginId ($constraint)"
	}

	public sealed class WrongVersion(
		pluginId: String,

		public override val readableString: String,
	) : ConstraintResult(pluginId, readableString) {
		public abstract val requiredPluginId: String
		public abstract val requiredPluginVersion: Version
		public abstract val constraint: Constraint
	}

	public class WrongNeededVersion(
		pluginId: String,

		public override val requiredPluginId: String,
		public override val requiredPluginVersion: Version,
		public override val constraint: Constraint,
	) : WrongVersion(
		pluginId,
		"Needs \"$requiredPluginId\" (constraint: $constraint), but incompatible version " +
			"$requiredPluginVersion was provided"
	) {
		override fun toString(): String =
			"Need version: $pluginId -> $requiredPluginId $constraint, found $requiredPluginVersion"
	}

	public class WrongWantedVersion(
		pluginId: String,

		public override val requiredPluginId: String,
		public override val requiredPluginVersion: Version,
		public override val constraint: Constraint,
	) : WrongVersion(
		pluginId,
		"Wants \"$requiredPluginId\" (constraint: $constraint), but incompatible version " +
			"$requiredPluginVersion was provided"
	) {
		override fun toString(): String =
			"Want version: $pluginId -> $requiredPluginId $constraint, found $requiredPluginVersion"
	}

	public sealed class FailedInnerConstraints(
		pluginId: String,

		public override val readableString: String,
	) : ConstraintResult(pluginId, readableString) {
		public abstract val requiredPluginId: String
		public abstract val innerResults: List<ConstraintResult>
	}

	public class NeededPluginFailedConstraints(
		pluginId: String,

		public override val requiredPluginId: String,
		public override val innerResults: List<ConstraintResult>,
	) : FailedInnerConstraints(
		pluginId,
		"\"$requiredPluginId\" is required but has ${innerResults.size} failed constraints:\n" +
			innerResults.joinToString("\n") { it.readableString.prependIndent(INDENT) }
	) {
		override fun toString(): String =
			"Needed failed constraints: $pluginId -> $requiredPluginId, ${innerResults.size} failures \n" +
				innerResults.joinToString("\n") { it.toString().prependIndent(INDENT) }
	}

	public class WantedPluginFailedConstraints(
		pluginId: String,

		public override val requiredPluginId: String,
		public override val innerResults: List<ConstraintResult>,
	) : FailedInnerConstraints(
		pluginId,
		"\"$requiredPluginId\" is wanted but has ${innerResults.size} failed constraints:\n" +
			innerResults.joinToString("\n") { it.readableString.prependIndent(INDENT) }
	) {
		override fun toString(): String =
			"Wanted failed constraints: $pluginId -> $requiredPluginId, ${innerResults.size} failures \n" +
				innerResults.joinToString("\n") { it.toString().prependIndent(INDENT) }
	}
}
