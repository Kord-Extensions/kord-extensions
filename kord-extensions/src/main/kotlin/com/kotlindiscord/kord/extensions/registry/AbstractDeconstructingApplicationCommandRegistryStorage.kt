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
public abstract class AbstractDeconstructingApplicationCommandRegistryStorage :
    RegistryStorage<Snowflake, ApplicationCommand<*>> {

    /**
     * Mapping of command-name to command-object.
     */
    private val commandMapping: MutableMap<String, ApplicationCommand<*>> = mutableMapOf()

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

    override suspend fun upsert(id: Snowflake, data: ApplicationCommand<*>) {
        commandMapping[data.name] = data
        upsert(id.asString, data.name)
    }

    override suspend fun read(id: Snowflake): ApplicationCommand<*>? {
        val name = read(id.asString) ?: return null
        return commandMapping[name]
    }

    override suspend fun delete(id: Snowflake): ApplicationCommand<*>? {
        val name = delete(id.asString) ?: return null
        return commandMapping[name]
    }

    override fun entryFlow(): Flow<RegistryStorage.StorageEntry<Snowflake, ApplicationCommand<*>>> = entries()
        .mapNotNull { commandMapping[it.value]?.let { cmd -> RegistryStorage.StorageEntry(Snowflake(it.key), cmd) } }
}
