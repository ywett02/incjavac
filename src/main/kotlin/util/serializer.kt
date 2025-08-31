package com.example.assignment.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.SetSerializer

fun <T> mapOfSetsSerializer(valueSerializer: KSerializer<T>): KSerializer<Map<T, Set<T>>> {
    return MapSerializer(valueSerializer, SetSerializer(valueSerializer))
}