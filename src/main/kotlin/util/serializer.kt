package com.example.assignment.util

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer

fun <K, V> mapOfSetsSerializer(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
): KSerializer<Map<K, Set<V>>> {
    return MapSerializer(keySerializer, SetSerializer(valueSerializer))
}

fun <K, V> mapSerializer(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>): KSerializer<Map<K, V>> {
    return MapSerializer(keySerializer, valueSerializer)
}