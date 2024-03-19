/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb

import com.kotlindiscord.kord.extensions.adapters.mongodb.db.AdaptedData
import com.kotlindiscord.kord.extensions.adapters.mongodb.db.Metadata
import com.kotlindiscord.kord.extensions.storage.StorageType
import dev.kord.common.entity.Snowflake
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.kotlinx.KotlinSerializerCodec

public val kordExCodecRegistry: CodecRegistry = CodecRegistries.fromCodecs(
	Metadata.codec,

	KotlinSerializerCodec.create<AdaptedData>(),

	KotlinSerializerCodec.create<DateTimePeriod>(),
	KotlinSerializerCodec.create<Instant>(),
	KotlinSerializerCodec.create<Snowflake>(),

	KotlinSerializerCodec.create<StorageType>(),
)
