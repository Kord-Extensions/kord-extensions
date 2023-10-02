/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb.codecs

import kotlinx.datetime.DateTimePeriod
import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext

public object DateTimePeriodCodec : Codec<DateTimePeriod> {
	override fun decode(reader: BsonReader, decoderContext: DecoderContext): DateTimePeriod =
		DateTimePeriod.parse(reader.readString())

	override fun encode(writer: BsonWriter, value: DateTimePeriod, encoderContext: EncoderContext) {
		writer.writeString(value.toString())
	}

	override fun getEncoderClass(): Class<DateTimePeriod> =
		DateTimePeriod::class.java
}
