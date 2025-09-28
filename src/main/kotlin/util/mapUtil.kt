package com.example.javac.incremental.util

fun <K, V> Map<K, Set<V>>.inverted(): Map<V, Set<K>> {
    val result = mutableMapOf<V, MutableSet<K>>()

    for ((root, deps) in this) {
        for (dep in deps) {
            result.computeIfAbsent(dep) { mutableSetOf() }.add(root)
        }
    }

    return result
}

fun <K, V> Map<K, Set<V>>.joinToString(transformKey: (K) -> String, transformValue: (V) -> String): String =
    buildString {
        for ((outerKey, innerSet) in this@joinToString) {
            append(transformKey(outerKey))
            append("\n")
            for (innerKey in innerSet) {
                append("  ${transformValue(innerKey)}")
                append("\n")
            }
        }
    }