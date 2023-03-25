/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.storage

import kotlinx.serialization.Serializable

/**
 * Sealed class representing the two types of storage - configuration and data.
 *
 * @property type Human-readable storage type name.
 */
@Serializable(with = StorageTypeSerializer::class)
public sealed class StorageType(public val type: String) {
    /** Configuration data. **/
    public object Config : StorageType("config")

    /** Long-term dynamic data. **/
    public object Data : StorageType("data")
}
