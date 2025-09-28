package com.example.javac.incremental.entity.serializer

import com.example.javac.incremental.entity.Graph
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class GraphSerializer<T>(
    valueSerializer: KSerializer<T>
) : KSerializer<Graph<T>> {

    private val mapSerializer = MapSerializer(valueSerializer, SetSerializer(valueSerializer))
    override val descriptor: SerialDescriptor = mapSerializer.descriptor

    override fun serialize(encoder: Encoder, value: Graph<T>) {
        val serializableMap = value.nodes
            .filter { (_, node) ->
                node.children.isNotEmpty()
            }.map { (_, node) ->
                node.value to node.children.map { it.value }.toSet()
            }.toMap()

        encoder.encodeSerializableValue(mapSerializer, serializableMap)
    }

    override fun deserialize(decoder: Decoder): Graph<T> {
        val serializableMap = decoder.decodeSerializableValue(mapSerializer)
        val graph = Graph<T>()

        serializableMap.forEach { (key, values) ->
            graph.add(key)

            values.forEach { value ->
                graph.addEdge(key, value)
            }
        }

        return graph
    }
}