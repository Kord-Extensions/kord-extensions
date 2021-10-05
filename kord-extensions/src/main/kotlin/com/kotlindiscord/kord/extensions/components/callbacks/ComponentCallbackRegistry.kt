package com.kotlindiscord.kord.extensions.components.callbacks

import com.kotlindiscord.kord.extensions.registry.DefaultLocalRegistryStorage
import com.kotlindiscord.kord.extensions.registry.RegistryStorage

/**
 * Very simple registry for keeping track of registered component callbacks.
 *
 * Intended to be extended for more advanced use-cases.
 */
public open class ComponentCallbackRegistry {
    /** Component callback storage, keyed by unique ID. **/
    public open val storage: RegistryStorage<String, ComponentCallback<*>> = DefaultLocalRegistryStorage()

    /** Register a public button callback to the given ID. **/
    public open suspend fun registerForPublicButton(id: String, callback: PublicButtonCallback): Unit =
        storage.set(id, callback)

    /** Register an ephemeral button callback to the given ID. **/
    public open suspend fun registerForEphemeralButton(id: String, callback: EphemeralButtonCallback): Unit =
        storage.set(id, callback)

    /** Register a public select menu callback to the given ID. **/
    public open suspend fun registerForPublicMenu(id: String, callback: PublicMenuCallback): Unit =
        storage.set(id, callback)

    /** Register an ephemeral select menu callback to the given ID. **/
    public open suspend fun registerForEphemeralMenu(id: String, callback: EphemeralMenuCallback): Unit =
        storage.set(id, callback)

    /** Get a generic component callback object for the given ID, throwing if it doesn't exist. **/
    public open suspend fun get(id: String): ComponentCallback<*> =
        storage.get(id) ?: error("No callback registered for ID: $id")

    /** Get a generic component callback object for the given ID, or null if it doesn't exist. **/
    public open suspend fun getOrNull(id: String): ComponentCallback<*>? =
        storage.get(id)

    /** Get a typed component callback object for the given ID, throwing if it doesn't exist or is the wrong type. **/
    public suspend inline fun <reified T : ComponentCallback<*>> getOfType(id: String): T {
        val callback = storage.get(id) ?: error("No callback registered for ID: $id")

        return callback as T
    }

    /** Get a typed component callback object for the given ID, or null if it doesn't exist or is the wrong type. **/
    public suspend inline fun <reified T : ComponentCallback<*>> getOfTypeOrNull(id: String): T? {
        val callback = storage.get(id)

        return callback as? T
    }
}
