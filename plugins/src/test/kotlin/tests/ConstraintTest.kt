/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package tests

import ManifestFixtures
import com.kotlindiscord.kord.extensions.plugins.PluginManager
import com.kotlindiscord.kord.extensions.plugins.constraints.ConstraintChecker
import com.kotlindiscord.kord.extensions.plugins.constraints.ConstraintResult
import io.github.z4kn4fein.semver.toVersion
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import types.FakePlugin

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConstraintTest {
	private val pluginManager = PluginManager<FakePlugin>(
		"types.FakePlugin",

		extraVersions = mapOf(
			"fake10" to "1.0.0".toVersion(),
			"fake15" to "1.5.0".toVersion(),
			"fake20" to "2.0.0".toVersion(),
			"fake25" to "2.5.0".toVersion(),
			"fake30" to "3.0.0".toVersion(),
			"fake35" to "3.5.0".toVersion(),
		)
	)

	private val constraintChecker = ConstraintChecker(pluginManager)

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `No problems with good constraints`() {
		val results = constraintChecker.checkAll(ManifestFixtures.good)

		assert(results.isEmpty()) {
			"Expected zero results, got $results"
		}
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `No problems with missing wanted dependency`() {
		val results = constraintChecker.checkAll(ManifestFixtures.missingWants)

		assert(results.isEmpty()) {
			"Expected zero results, got $results"
		}
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Problems found with bad wants`() {
		val results = constraintChecker.checkAll(ManifestFixtures.badWants)

		assert(results.isNotEmpty()) {
			"Expected at least one result, got zero"
		}

		val badResults = results.values.flatten().filter {
			it !is ConstraintResult.WrongWantedVersion &&
				it !is ConstraintResult.FailedInnerConstraints
		}

		assert(badResults.isEmpty()) {
			"Expected only wanted version/inner failures, additionally found $badResults"
		}
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Problems found with bad needs`() {
		val results = constraintChecker.checkAll(ManifestFixtures.badNeeds)

		assert(results.isNotEmpty()) {
			"Expected at least one result, got zero"
		}

		val badResults = results.values.flatten().filter {
			it !is ConstraintResult.WrongNeededVersion &&
				it !is ConstraintResult.FailedInnerConstraints
		}

		assert(badResults.isEmpty()) {
			"Expected only needed version/inner failures results, additionally found $badResults"
		}
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Problems found with missing needed dependency`() {
		val results = constraintChecker.checkAll(ManifestFixtures.missingNeeds)

		assert(results.isNotEmpty()) {
			"Expected at least one result, got zero"
		}

		val badResults = results.values.flatten().filter {
			it !is ConstraintResult.Missing &&
				it !is ConstraintResult.FailedInnerConstraints
		}

		assert(badResults.isEmpty()) {
			"Expected only missing/inner failures, additionally found $badResults"
		}
	}

	@Test
	@Execution(ExecutionMode.CONCURRENT)
	fun `Recursive checking with errors`() {
		val results = constraintChecker.checkPlugin("one", ManifestFixtures.badWants)

		assert(results.isNotEmpty()) {
			"Expected at least one result, got zero"
		}

		val goodResults = results.filter {
			it is ConstraintResult.FailedInnerConstraints
		}

		assert(goodResults.isNotEmpty()) {
			"Expected inner failures results, but found zero; results: $results"
		}
	}
}
