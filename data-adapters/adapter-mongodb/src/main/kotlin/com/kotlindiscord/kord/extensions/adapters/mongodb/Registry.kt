package com.kotlindiscord.kord.extensions.adapters.mongodb

import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.InstantCodec
import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.SnowflakeCodec
import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.StorageTypeCodec
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

public val kordExCodecRegistry: CodecRegistry = CodecRegistries.fromCodecs(
	InstantCodec(),
	SnowflakeCodec(),
	StorageTypeCodec(),
)
