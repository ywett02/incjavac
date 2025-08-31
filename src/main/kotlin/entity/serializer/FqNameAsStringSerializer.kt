package com.example.assignment.entity.serializer

import com.example.assignment.entity.FqName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder


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