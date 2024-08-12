/*
 * Copyrighted (Kord Extensions, 2024). Licensed under the EUPL-1.2
 * with the specific provision (EUPL articles 14 & 15) that the
 * applicable law is the (Republic of) Irish law and the Jurisdiction
 * Dublin.
 * Any redistribution must include the specific provision above.
 */

package dev.kordex.modules.data.mongodb

import dev.kord.common.entity.Snowflake
import dev.kordex.core.storage.StorageType
import dev.kordex.modules.data.mongodb.db.AdaptedData
import dev.kordex.modules.data.mongodb.db.Metadata
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
