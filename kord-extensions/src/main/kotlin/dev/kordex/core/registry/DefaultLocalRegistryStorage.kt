/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.kordex.core.registry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

/**
 * Default "local" implementation of [RegistryStorage] which internally
 * uses a mutableMap.
 */
public open class DefaultLocalRegistryStorage<K, T> : RegistryStorage<K, T> {

	protected open val registry: MutableMap<K, T> = mutableMapOf()

	override suspend fun register(data: T) {
		// we don't need to do anything here
	}

	override suspend fun set(id: K, data: T) {
		registry[id] = data
	}

	override suspend fun get(id: K): T? = registry[id]

	override suspend fun remove(id: K): T? = registry.remove(id)

	override fun entryFlow(): Flow<RegistryStorage.StorageEntry<K, T>> {
		return registry.entries
			.asFlow()
			.map { RegistryStorage.StorageEntry(it.key, it.value) }
	}

	override fun constructUniqueIdentifier(data: T): String = data.hashCode().toString()
}
