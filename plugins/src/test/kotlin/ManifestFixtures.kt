import com.kotlindiscord.kord.extensions.plugins.types.PluginConstraints
import com.kotlindiscord.kord.extensions.plugins.types.PluginManifest
import io.github.z4kn4fein.semver.constraints.toConstraint
import io.github.z4kn4fein.semver.toVersion

object ManifestFixtures {
	val good: Map<String, PluginManifest> = mapOf(
		"one" to PluginManifest(
			classRef = "a.b.OneKt",
			id = "one",
			description = "Test Plugin 1",
			license = "MPL-2.0",
			name = "Test Plugin 1",
			version = "0.0.1".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"two" to ">= 0.0.1".toConstraint(),
					"fake20" to "> 1.0.0".toConstraint()
				),

				wants = mapOf(
					"three" to "> 0.0.2".toConstraint()
				)
			)
		),

		"two" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.2".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"one" to "*".toConstraint(),
					"fake35" to "> 2.5.0".toConstraint()
				),

				wants = mapOf(
					"three" to "*".toConstraint()
				)
			)
		),

		"three" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.3".toVersion()
		),
	)

	val missingNeeds: Map<String, PluginManifest> = mapOf(
		"one" to PluginManifest(
			classRef = "a.b.OneKt",
			id = "one",
			description = "Test Plugin 1",
			license = "MPL-2.0",
			name = "Test Plugin 1",
			version = "0.0.1".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"two" to ">= 0.0.1".toConstraint(),
					"fake20" to "> 1.0.0".toConstraint(),
					"three" to "> 0.0.2".toConstraint()
				),
			)
		),

		"two" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.2".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"one" to "*".toConstraint(),
					"fake35" to "> 2.5.0".toConstraint(),
					"three" to "*".toConstraint()
				),
			)
		),
	)
	val missingWants: Map<String, PluginManifest> = mapOf(
		"one" to PluginManifest(
			classRef = "a.b.OneKt",
			id = "one",
			description = "Test Plugin 1",
			license = "MPL-2.0",
			name = "Test Plugin 1",
			version = "0.0.1".toVersion(),

			constraints = PluginConstraints(
				wants = mapOf(
					"two" to ">= 0.0.1".toConstraint(),
					"fake20" to "> 1.0.0".toConstraint(),
					"three" to "> 0.0.2".toConstraint()
				),
			)
		),

		"two" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.2".toVersion(),

			constraints = PluginConstraints(
				wants = mapOf(
					"one" to "*".toConstraint(),
					"fake35" to "> 2.5.0".toConstraint(),
					"three" to "*".toConstraint()
				),
			)
		),
	)

	val badWants: Map<String, PluginManifest> = mapOf(
		"one" to PluginManifest(
			classRef = "a.b.OneKt",
			id = "one",
			description = "Test Plugin 1",
			license = "MPL-2.0",
			name = "Test Plugin 1",
			version = "0.0.1".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"two" to ">= 0.0.1".toConstraint(),
					"fake20" to "> 1.0.0".toConstraint()
				),

				wants = mapOf(
					"three" to "> 0.0.4".toConstraint()
				)
			)
		),

		"two" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.2".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"one" to "*".toConstraint(),
					"fake35" to "> 2.5.0".toConstraint()
				),

				wants = mapOf(
					"fake35" to "< 3.0.0".toConstraint()
				)
			)
		),

		"three" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.3".toVersion()
		),
	)

	val badNeeds: Map<String, PluginManifest> = mapOf(
		"one" to PluginManifest(
			classRef = "a.b.OneKt",
			id = "one",
			description = "Test Plugin 1",
			license = "MPL-2.0",
			name = "Test Plugin 1",
			version = "0.0.1".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"two" to ">= 0.0.1".toConstraint(),
					"three" to "> 0.0.4".toConstraint(),

					"fake20" to "> 1.0.0".toConstraint()
				),
			)
		),

		"two" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.2".toVersion(),

			constraints = PluginConstraints(
				needs = mapOf(
					"one" to "*".toConstraint(),
					"fake35" to "< 3.0.0".toConstraint(),
				),
			)
		),

		"three" to PluginManifest(
			classRef = "a.b.TwoKt",
			id = "two",
			description = "Test Plugin 2",
			license = "MPL-2.0",
			name = "Test Plugin 2",
			version = "0.0.3".toVersion()
		),
	)
}
