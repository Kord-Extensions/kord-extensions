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
	KotlinSerializerCodec.create<StorageType.Config>(),
	KotlinSerializerCodec.create<StorageType.Data>(),
)
