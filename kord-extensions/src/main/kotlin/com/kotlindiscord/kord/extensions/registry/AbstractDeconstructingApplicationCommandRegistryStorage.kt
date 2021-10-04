package com.kotlindiscord.kord.extensions.registry

import com.kotlindiscord.kord.extensions.commands.application.ApplicationCommand
import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

/**
 * Abstract class which can be used to implement a simple networked key-value based registry storage
 * for [ApplicationCommand]s.
 *
 * For simplicity the parameter / return types of the abstract methods are all [String]s.
 */
public abstract class AbstractDeconstructingApplicationCommandRegistryStorage<T : ApplicationCommand<*>> :
    RegistryStorage<Snowflake, T> {

    /**
     * Mapping of command-key to command-object.
     */
    protected open val commandMapping: MutableMap<String, T> = mutableMapOf()

    /**
     * Upserts simplified data.
     * The key is the command id, which is returned by the create request from discord.
     * The value is the command name, which must be unique across the registry.
     */
    protected abstract suspend fun upsert(key: String, value: String)

    /**
     * Reads simplified data from the storage.
     *
     * The key is the command id.
     *
     * Returns the command name associated with this key.
     */
    protected abstract suspend fun read(key: String): String?

    /**
     * Deletes and returns simplified data.
     *
     * The key is the command id.
     *
     * Returns the command name associated with this key.
     */
    protected abstract suspend fun delete(key: String): String?

    /**
     * Returns all entries in this registry as simplified data.
     *
     * The key is the command id.
     * The value is the command name associated with this key.
     */
    protected abstract fun entries(): Flow<RegistryStorage.StorageEntry<String, String>>

    override fun constructUniqueIdentifier(data: T): String = "${data.name}-${data.type.value}-${data.guildId ?: 0}"

    override suspend fun register(data: T) {
        commandMapping[constructUniqueIdentifier(data)] = data
    }

    override suspend fun set(id: Snowflake, data: T) {
        val key = constructUniqueIdentifier(data)
        commandMapping[key] = data
        upsert(id.asString, key)
    }

    override suspend fun get(id: Snowflake): T? {
        val key = read(id.asString) ?: return null
        return commandMapping[key]
    }

    override suspend fun remove(id: Snowflake): T? {
        val key = delete(id.asString) ?: return null
        return commandMapping[key]
    }

    override fun entryFlow(): Flow<RegistryStorage.StorageEntry<Snowflake, T>> = entries()
        .mapNotNull { commandMapping[it.value]?.let { cmd -> RegistryStorage.StorageEntry(Snowflake(it.key), cmd) } }
}
