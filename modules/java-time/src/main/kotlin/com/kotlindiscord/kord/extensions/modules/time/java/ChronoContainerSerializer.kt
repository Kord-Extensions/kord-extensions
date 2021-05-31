package com.kotlindiscord.kord.extensions.modules.time.java

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.temporal.ChronoUnit

/**
 * Serializer that converts [ChronoContainer]s between comma-separated `name:amount` string pairs.
 */
public class ChronoContainerSerializer : KSerializer<ChronoContainer> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ChronoContainer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ChronoContainer {
        val container = ChronoContainer()
        val string = decoder.decodeString()

        string.split(",").forEach {
            val (unitName, amount) = it.split(":")
            val unit = ChronoUnit.valueOf(unitName)

            container.plus(amount.toLong(), unit)
        }

        return container
    }

    override fun serialize(encoder: Encoder, value: ChronoContainer) {
        val string = value.values
            .map { (unit, amount) -> "${unit.name}:$amount" }
            .joinToString(",")

        encoder.encodeString(string)
    }
}
