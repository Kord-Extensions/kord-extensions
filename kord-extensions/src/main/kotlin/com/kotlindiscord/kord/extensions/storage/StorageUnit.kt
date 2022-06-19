package com.kotlindiscord.kord.extensions.storage

import com.kotlindiscord.kord.extensions.koin.KordExKoinComponent
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import org.koin.core.component.inject
import kotlin.reflect.KClass

/**
 * Data class representing a storage unit. Storage units represent specific, single units of data, and explain how
 * to store, retrieve and serialize that data.
 *
 * Storage units instruct the data adapters, explaining exactly what needs to be done. However, those adapters are
 * free to handle the storage as they feel they need to.
 */
@Suppress("DataClassContainsFunctions")
public data class StorageUnit<T : Data>(
    /** The type of data to store. **/
    public val storageType: StorageType,

    /** The namespace - usually a plugin or extension ID. Represents a folder for file-backed storage. **/
    public val namespace: String,

    /** The identifier - usually a specific category or name. Represents a filename for file-backed storage. **/
    public val identifier: String,

    /** The classobj representing your data - usually retrieved via `MyDataClass::class`. **/
    public val dataType: KClass<T>
) : KordExKoinComponent {
    /** Storage unit key - used to construct paths, or just as a string reference to this storage unit. **/
    public val unitKey: String = "${storageType.type}/$namespace/$identifier"

    private val dataAdapter: DataAdapter by inject()

    @OptIn(InternalSerializationApi::class)
    internal val serializer: KSerializer<T> = dataType.serializer()

    /**
     * Convenience function, allowing you to delete the data represented by this storage unit.
     *
     * @see DataAdapter.delete
     */
    public suspend fun delete(): Boolean =
        dataAdapter.delete(this)

    /**
     * Convenience function, allowing you to retrieve the data represented by this storage unit.
     *
     * @see DataAdapter.get
     */
    public suspend fun get(): T? =
        dataAdapter.get(this)

    /**
     * Convenience function, allowing you to reload the data represented by this storage unit.
     *
     * @see DataAdapter.reload
     */
    public suspend fun reload(): T? =
        dataAdapter.reload(this)

    /**
     * Convenience function, allowing you to save the cached data represented by this storage unit.
     *
     * @see DataAdapter.save
     */
    public suspend fun save() {
        dataAdapter.save(this)
    }

    /**
     * Convenience function, allowing you to save the given data object, as represented by this storage unit.
     *
     * @see DataAdapter.save
     */
    public suspend fun save(data: T) {
        dataAdapter.save(this, data)
    }

    override fun toString(): String = unitKey
}
