package com.kotlindiscord.kord.extensions.types

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map

/**
 * Default "local" implementation of [RegistryStorage] which internally
 * uses a mutableMap.
 */
public class DefaultLocalRegistryStorage<K, T> : RegistryStorage<K, T> {

    private val registry: MutableMap<K, T> = mutableMapOf()

    override suspend fun upsert(id: K, data: T) {
        registry[id] = data
    }

    override suspend fun read(id: K): T? = registry[id]

    override suspend fun delete(id: K): T? = registry.remove(id)

    override fun entryFlow(): Flow<RegistryStorage.StorageEntry<K, T>> {
        return registry.entries
            .asFlow()
            .map { RegistryStorage.StorageEntry(it.key, it.value) }
    }
}
