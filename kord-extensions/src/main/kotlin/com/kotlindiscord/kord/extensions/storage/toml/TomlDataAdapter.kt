/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:Suppress("UNCHECKED_CAST")

package com.kotlindiscord.kord.extensions.storage.toml

import com.kotlindiscord.kord.extensions.storage.Data
import com.kotlindiscord.kord.extensions.storage.DataAdapter
import com.kotlindiscord.kord.extensions.storage.StorageUnit
import com.kotlindiscord.kord.extensions.storage.storageFileRoot
import net.peanuuutz.tomlkt.Toml
import java.io.File
import kotlin.io.path.div

/**
 * Standard data adapter class implementing the TOML format. Stores TOML files in folders.
 *
 * This is a pretty simple implementation, so it's a good example to use when writing your own data adapters.
 */
public open class TomlDataAdapter : DataAdapter() {
    /** Take the unit key from a storage unit, and turn it into a file. **/
    internal val StorageUnit<*>.file: File
        get() = (storageFileRoot / "${this.unitKey}.toml").toFile()

    override suspend fun <R : Data> delete(unit: StorageUnit<R>): Boolean {
        cache.remove(unit)

        val file = unit.file

        if (file.exists()) {
            return file.delete()
        }

        return false
    }

    override suspend fun <R : Data> get(unit: StorageUnit<R>): R? {
        if (cache.containsKey(unit)) {
            return cache[unit]!! as R
        }

        return reload(unit)
    }

    override suspend fun <R : Data> reload(unit: StorageUnit<R>): R? {
        val file = unit.file

        if (file.exists()) {
            val result: R = Toml.decodeFromString(unit.serializer, file.readText())

            cache[unit] = result
        }

        return cache[unit] as R?
    }

    override suspend fun <R : Data> save(unit: StorageUnit<R>, data: R) {
        cache[unit] = data

        val file = unit.file

        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        file.writeText(Toml.encodeToString(unit.serializer, data))
    }

    override suspend fun <R : Data> save(unit: StorageUnit<R>) {
        val data = get(unit) ?: return
        val file = unit.file

        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
        }

        file.writeText(Toml.encodeToString(unit.serializer, data))
    }

    override suspend fun reloadAll() {
        cache.keys.forEach { reload(it) }
    }

    override suspend fun saveAll() {
        cache.keys.forEach { save(it) }
    }
}
