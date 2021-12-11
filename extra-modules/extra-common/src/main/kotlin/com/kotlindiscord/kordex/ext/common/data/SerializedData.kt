/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

@file:OptIn(ExperimentalPathApi::class)

package com.kotlindiscord.kordex.ext.common.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.*

/**
 * Abstract class representing data that's serialized to disk. Uses `kotlinx.serialization`.
 *
 * By default, this class assumes you want to use JSON, and will serialize to JSON files on disk. You can change this
 * behaviour in your subclasses by overriding the `decode` and `encode` functions and changing the `fileExtension`
 * constructor parameter.
 *
 * This class saves data in `./data/{dataFolder}/{baseName}.{fileExtension}`. You always need to provide the
 * `baseName` and `dataFolder` parameters, but `fileExtension` will default to `"json"` if you don't set it. We
 * recommend providing these as part of the superclass invocation rather than as constructor parameters in your
 * subclass, if that's appropriate.
 *
 * If the file doesn't exist or is empty, then it will automatically be filled with the data class created by the
 * `defaultBuilder` function at load time. Deserialization errors will not result in the file being overwritten with
 * default data, you'll need to handle that yourself if it's what you need.
 *
 * **Note:** You will need to manually call the `save` and `load` functions. This class is intended to be used with a
 * single instance of a mutable data class, with functions provided by a separate interface that you implement to
 * access and modify the data in the data class. For example, if your data class tracks an `enabled` value, you should
 * implement either a `getEnabled()` and `setEnabled(bool)` function, or a `val` with custom getters and setters - but
 * both options **must** call `save` as appropriate when data is modified!
 *
 * @param T TypeVar representing the serializable class you plan to serialize.
 * @param baseName Filename, excluding extension.
 * @param dataFolder Folder within the `./data` folder to place the file within.
 * @param serializerObj Serializer for your data class - pass `serializer()` if you don't have a custom one.
 * @param defaultBuilder Builder function or lambda that creates a new instance of your data class, with defaults set
 *                       up. If all of the parameters in your data class have default values, you can pass the
 *                       constructor using `::T` instead.
 * @param fileExtension File extension, defaulting to "json" - if you're using a custom format, you can change this
 *                      in your subclass.
 */
@Suppress("UnnecessaryAbstractClass")  // Literally an API class
public abstract class SerializedData<T : Any>(
    baseName: String,
    dataFolder: String,
    private val serializerObj: KSerializer<T>,
    defaultBuilder: () -> T,
    fileExtension: String = "json"
) {
    /** Stored data class, which will be the result of `defaultBuilder()` if the file doesn't exist or contain data. **/
    var data: T = defaultBuilder()
        private set

    private val path: Path = Path.of("data", dataFolder, "$baseName.$fileExtension")

    /** Load up data from file, returning the decoded data class instance. **/
    fun load(): T {
        if (path.exists()) {
            val text = path.readText(Charsets.UTF_8)

            if (text.trim().isNotEmpty()) {
                data = decode(path.readText(Charsets.UTF_8))
            } else {
                save()
            }
        } else {
            save()
        }

        return data
    }

    /** Save the data to file, ensuring the path and file exists. **/
    fun save() {
        ensurePath()

        path.writeText(encode(data))
    }

    /** Decode an instance of the data class from a given string. This does not store the result. **/
    open fun decode(string: String): T = Json.decodeFromString(serializerObj, string)

    /** Encode a given instance of the data class to a string. **/
    open fun encode(obj: T): String = Json.encodeToString(serializerObj, obj)

    private fun ensurePath() {
        if (path.notExists()) {
            if (path.parent.notExists()) {
                path.parent.createDirectories()
            }

            path.createFile()
        }
    }
}
