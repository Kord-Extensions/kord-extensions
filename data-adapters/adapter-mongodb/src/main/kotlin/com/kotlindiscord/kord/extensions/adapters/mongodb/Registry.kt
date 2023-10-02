package com.kotlindiscord.kord.extensions.adapters.mongodb

import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.*
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

public val kordExCodecRegistry: CodecRegistry = CodecRegistries.fromCodecs(
	DateTimePeriodCodec,
	InstantCodec,
	SnowflakeCodec,
	StorageTypeCodec,

	// Required b/c the BSON codec library doesn't support standard polymorphism.
	ConfigStorageTypeCodec,
	DataStorageTypeCodec,
)
