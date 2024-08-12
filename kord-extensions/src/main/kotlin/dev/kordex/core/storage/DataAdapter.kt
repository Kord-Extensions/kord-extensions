/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.core.storage

/**
 * Abstract class representing a data adapter. In charge of caching, storing and loading data for extensions,
 * plugins, and other parts of your bot.
 *
 * This class exists because it's intended for you to be able to create your own data adapters. As your bot is
 * configured to use a single, global adapter, this allows you to switch up how your configuration is stored,
 * as long as the eventual data translation happens via `kotlinx.serialization`.
 *
 * As storage units may provide contextual information but may not necessarily point to different actual data objects,
 * data adapters also include the concept of data IDs. These IDs are identifiers that provide a mapping between storage
 * units and the actual data objects they refer to. As an example, a file-backed data adapter will likely use the path
 * to the file here - so all storage units pointing to the same file will point at the same data object.
 *
 * Data IDs allow you to point at contextual data sources when required, allowing for quite a few different
 * possibilities in configuration. For example, different guilds, users, and so on may have their own separate
 * configurations, if you so desire. It's up to your extensions to work with this system, and only provide the extra
 * storage unit context that makes sense for the specific use-case.
 *
 * @param ID A typevar representing what you use to identify specific instances of data storage, referred to as data
 * IDs. This will often be a string, but you can use anything that can reasonably be used as the key for a map.
 * File-based data adapters will likely just use the file path here.
 */
public abstract class DataAdapter<ID : Any> {
	/** A simple map that maps data IDs to data objects. **/
	protected open val dataCache: MutableMap<ID, Data> = mutableMapOf()

	/** A simple map that maps storage units to your data IDs. **/
	protected open val unitCache: MutableMap<StorageUnit<*>, ID> = mutableMapOf()

	/**
	 * Entirely removes the data represented by the given storage unit from the [dataCache], the disk if this is an
	 * adapter that stores data in files, or from whatever relevant persistent storage is being used.
	 *
	 * @return Whether the data was deleted - should return `false` if it didn't exist.
	 */
	public abstract suspend fun <R : Data> delete(unit: StorageUnit<R>): Boolean

	/**
	 * Retrieve and return the data object represented by the given storage unit.
	 *
	 * This function should attempt to retrieve from the [dataCache] first, and return the result of [reload] if it
	 * isn't present there.
	 *
	 * @return The loaded data if it was found, `null` otherwise.
	 */
	public abstract suspend fun <R : Data> get(unit: StorageUnit<R>): R?

	/**
	 * Retrieve the data object represented by the given storage unit, or store the data object returned by the
	 * callback if no respective data could be found.
	 *
	 * This is similar to the `getOrDefault` you'd find on collections, but it also saves the default for you. You
	 * can use an elvis operator (`?:`) if you don't want to save.
	 *
	 * This function takes a lambda so that data objects aren't created unless they're needed.
	 *
	 * @return The stored data, or the data you passed if there was nothing stored.
	 */
	public open suspend fun <R : Data> getOrSaveDefault(unit: StorageUnit<R>, data: suspend () -> R): R =
		get(unit) ?: save(unit, data())

	/**
	 * Retrieve the data represented by the given storage unit from persistent storage, storing it in the [dataCache]
	 * and returning it if it was found.
	 *
	 * @return The loaded data if it was found, `null` otherwise.
	 */
	public abstract suspend fun <R : Data> reload(unit: StorageUnit<R>): R?

	/**
	 * Save the cached data represented by the given storage unit to persistent storage, creating any files and folders
	 * as needed.
	 *
	 * @return The saved data if it was found, `null` otherwise.
	 */
	public abstract suspend fun <R : Data> save(unit: StorageUnit<R>): R?

	/**
	 * Save the given data represented by the given storage unit to persistent storage, creating any files and folders
	 * as needed, and storing the given data object in the [dataCache].
	 *
	 * @return The saved data.
	 */
	public abstract suspend fun <R : Data> save(unit: StorageUnit<R>, data: R): R

	/**
	 * Reload all data objects stored in [dataCache] by calling [reload] against each storage unit.
	 */
	public open suspend fun reloadAll() {
		unitCache.keys.forEach { reload(it) }
	}

	/**
	 * Save all data objects stored in [dataCache] to persistent storage by calling [save] against each.
	 */
	public open suspend fun saveAll() {
		unitCache.keys.forEach { save(it) }
	}

	/**
	 * Convenience function for removing a storage unit from both caches, if required. Will only remove a stored data
	 * object if all storage units referencing it are removed.
	 */
	protected open suspend fun removeFromCache(unit: StorageUnit<*>) {
		val dataId = unitCache.remove(unit) ?: return
		val removeData = unitCache.filterValues { it == dataId }.isEmpty()

		if (removeData) {
			dataCache.remove(dataId)
		}
	}
}
