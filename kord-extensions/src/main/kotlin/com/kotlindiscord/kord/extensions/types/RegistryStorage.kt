package com.kotlindiscord.kord.extensions.types

import kotlinx.coroutines.flow.Flow

public interface RegistryStorage<K, T> {

    public suspend fun create(id: K, data: T)

    public suspend fun read(id: K): T?

    public suspend fun delete(id: K): T?

    public fun entryFlow(): Flow<StorageEntry<K, T>>

    public data class StorageEntry<K, V>(val key: K, val value: V)
}