/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.codecs

import com.kotlindiscord.kord.extensions.storage.StorageType
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

public class StorageTypeCodec : Codec<StorageType> {
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): StorageType =
        when (val string = reader.readString()) {
            StorageType.Config.type -> StorageType.Config
            StorageType.Data.type -> StorageType.Data

            else -> error("Unknown storage type: $string")
        }

    override fun encode(writer: BsonWriter, value: StorageType, encoderContext: EncoderContext) {
        writer.writeString(value.type)
    }

    override fun getEncoderClass(): Class<StorageType> =
        StorageType::class.java
}
