/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.storage

/**
 * Abstract class representing a data adapter. In charge of caching, storing and loading data for extensions,
 * plugins, and other parts of your bot.
 *
 * This class exists because it's intended for you to be able to create your own data adapters. As your bot is
 * configured to use a single, global adapter, this allows you to switch up how your configuration is stored,
 * as long as the eventual data translation happens via `kotlinx.serialization`.
 */
public abstract class DataAdapter {
    /** A simple map for in-memory caching of data. Please use this in your implementations, for performance. **/
    protected open val cache: MutableMap<StorageUnit<*>, Data> = mutableMapOf()

    /**
     * Entirely removes the data represented by the given storage unit from the [cache], the disk if this is an
     * adapter that stores data in files, or from whatever relevant persistent storage is being used.
     *
     * @return Whether the data was deleted - should return `false` if it didn't exist.
     */
    public abstract suspend fun <R : Data> delete(unit: StorageUnit<R>): Boolean

    /**
     * Retrieve and return the data object represented by the given storage unit.
     *
     * This function should attempt to retrieve from the [cache] first, and return the result of [reload] if it
     * isn't present there.
     *
     * @return The loaded data if it was found, `null` otherwise.
     */
    public abstract suspend fun <R : Data> get(unit: StorageUnit<R>): R?

    /**
     * Retrieve the data represented by the given storage unit from persistent storage, storing it in the [cache] and
     * returning it if it was found.
     *
     * @return The loaded data if it was found, `null` otherwise.
     */
    public abstract suspend fun <R : Data> reload(unit: StorageUnit<R>): R?

    /**
     * Save the cached data represented by the given storage unit to persistent storage, creating any files and folders
     * as needed.
     */
    public abstract suspend fun <R : Data> save(unit: StorageUnit<R>)

    /**
     * Save the given data represented by the given storage unit to persistent storage, creating any files and folders
     * as needed, and storing the given data object in the [cache].
     */
    public abstract suspend fun <R : Data> save(unit: StorageUnit<R>, data: R)

    /**
     * Reload all data objects stored in [cache] by calling [reload] against each storage unit.
     */
    public abstract suspend fun reloadAll()

    /**
     * Save all data objects stored in [cache] to persistent storage by calling [save] against each.
     */
    public abstract suspend fun saveAll()
}
