package com.example.assignment

fun Map<String, Set<String>>.joinToString(): String =
    buildString {
        for ((outerKey, innerSet) in this@joinToString) {
            append(outerKey)
            append("\n")
            for (innerKey in innerSet) {
                append("  $innerKey")
                append("\n")
            }
        }
    }