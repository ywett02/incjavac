package com.example.assignment.util

import com.example.assignment.entity.FqName
import java.io.File

fun <K, V> Map<K, Set<V>>.inverted(): Map<V, Set<K>> {
    val result = mutableMapOf<V, MutableSet<K>>()

    for ((root, deps) in this) {
        for (dep in deps) {
            result.computeIfAbsent(dep) { mutableSetOf() }.add(root)
        }
    }

    return result
}

//TODO remove
fun Map<FqName, Set<FqName>>.joinToString(): String =
    buildString {
        for ((outerKey, innerSet) in this@joinToString) {
            append(outerKey.toString())
            append("\n")
            for (innerKey in innerSet) {
                append("  ${innerKey.toString()}")
                append("\n")
            }
        }
    }

//TODO remove
fun Map<File, Set<FqName>>.joinToString2(): String =
    buildString {
        for ((outerKey, innerSet) in this@joinToString2) {
            append(outerKey.absolutePath)
            append("\n")
            for (innerKey in innerSet) {
                append("  ${innerKey.toString()}")
                append("\n")
            }
        }
    }