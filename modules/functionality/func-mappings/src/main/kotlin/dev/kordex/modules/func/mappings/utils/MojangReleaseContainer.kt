/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.func.mappings.utils

import me.shedaniel.linkie.namespaces.MojangNamespace
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

/** Mojang release container, allowing for retrieval of various Mojang mappings release versions. **/
object MojangReleaseContainer {
	/**
	 * A wrapper for [MojangNamespace.latestRelease], a private field.
	 */
	var latestRelease: String
		get() = latestReleaseProperty.getter.call()
		set(value) = latestReleaseProperty.setter.call(value)

	/**
	 * A wrapper for [MojangNamespace.latestSnapshot], a private field.
	 */
	var latestSnapshot: String
		get() = latestSnapshotProperty.getter.call()
		set(value) = latestSnapshotProperty.setter.call(value)

	@Suppress("UNCHECKED_CAST")
	private val latestSnapshotProperty by lazy {
		MojangNamespace::class.declaredMemberProperties
			.first { it.name == "latestSnapshot" }
			.also { it.isAccessible = true } as KMutableProperty1<MojangNamespace, String>
	}

	@Suppress("UNCHECKED_CAST")
	private val latestReleaseProperty by lazy {
		MojangNamespace::class.declaredMemberProperties
			.first { it.name == "latestRelease" }
			.also { it.isAccessible = true } as KMutableProperty1<MojangNamespace, String>
	}
}
