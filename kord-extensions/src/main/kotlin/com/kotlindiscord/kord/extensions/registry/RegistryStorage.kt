package com.kotlindiscord.kord.extensions.registry

import kotlinx.coroutines.flow.Flow

/**
 * Interface for interaction based registries like ComponentRegistry and ApplicationCommandRegistry.
 *
 * The purpose of this interface is to provide a generic way to store Components/ApplicationCommands
 * in a dynamic manner.
 */
public interface RegistryStorage<K, T> {

    /**
     * Lets the registry know about the specified type [T], this may store the object in a local map,
     * which is used for reconstructing later.
     */
    public suspend fun register(data: T)

    /**
     * Creates or updates an existing entry at the given unique key.
     *
     * This may deconstruct the given data and only persists a partial object.
     */
    public suspend fun set(id: K, data: T)

    /**
     * Reads a value from the registry at the given key.
     *
     * This may reconstruct the data from a partial object.
     */
    public suspend fun get(id: K): T?

    /**
     * Deletes a value from the registry with the given key.
     *
     * The return value may be a reconstructed object from partial data.
     */
    public suspend fun remove(id: K): T?

    /**
     * Creates a flow of all entries in this registry.
     *
     * The objects in this flow may be reconstructed from partial data.
     */
    public fun entryFlow(): Flow<StorageEntry<K, T>>

    /**
     * Constructs a unique key for the given data.
     */
    public fun constructUniqueIdentifier(data: T): String

    /**
     * Data class to represent an entry in the [RegistryStorage].
     */
    public data class StorageEntry<K, V>(
        /**
         * The key of this entry.
         */
        val key: K,
        /**
         * The value of this entry.
         */
        val value: V
    )
}
