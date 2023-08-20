/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kotlindiscord.kord.extensions.adapters.mongodb

import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.InstantCodec
import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.SnowflakeCodec
import com.kotlindiscord.kord.extensions.adapters.mongodb.codecs.StorageTypeCodec
import com.kotlindiscord.kord.extensions.utils.env
import com.mongodb.MongoClientSettings
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry

internal val MONGODB_URI: String = env("ADAPTER_MONGODB_URI")

public val kordExCodecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
	CodecRegistries.fromCodecs(
		InstantCodec(),
		SnowflakeCodec(),
		StorageTypeCodec(),
	),

	MongoClientSettings.getDefaultCodecRegistry(),
)
