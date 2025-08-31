package com.example.assignment.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class FqName(val id: String)

object FqNameAsStringSerializer : KSerializer<FqName> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FqName", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FqName) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): FqName {
        return FqName(decoder.decodeString())
    }
}