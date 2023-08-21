/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.codecs

import kotlinx.datetime.Instant
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

public object InstantCodec : Codec<Instant> {
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Instant =
        Instant.parse(reader.readString())

    override fun encode(writer: BsonWriter, value: Instant, encoderContext: EncoderContext) {
        writer.writeString(value.toString())
    }

    override fun getEncoderClass(): Class<Instant> =
        Instant::class.java
}
