package com.kotlindiscord.kord.extensions.storage

/**
 * Sealed class representing the two types of storage - configuration and data.
 *
 * @property type Human-readable storage type name.
 */
public sealed class StorageType(public val type: String) {
    /** Configuration data. **/
    public object Config : StorageType("config")

    /** Long-term dynamic data. **/
    public object Data : StorageType("data")
}
