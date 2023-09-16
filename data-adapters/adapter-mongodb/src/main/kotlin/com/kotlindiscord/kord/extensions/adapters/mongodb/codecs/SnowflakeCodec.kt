/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.codecs

import dev.kord.common.entity.Snowflake
import org.bson.BsonInvalidOperationException
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

public object SnowflakeCodec : Codec<Snowflake> {
    override fun decode(reader: BsonReader, decoderContext: DecoderContext): Snowflake = try {
	    Snowflake(reader.readString())
    } catch (e: BsonInvalidOperationException) {
	    Snowflake(reader.readInt64())
    }

    override fun encode(writer: BsonWriter, value: Snowflake, encoderContext: EncoderContext) {
        writer.writeString(value.toString())
    }

    override fun getEncoderClass(): Class<Snowflake> =
        Snowflake::class.java
}
