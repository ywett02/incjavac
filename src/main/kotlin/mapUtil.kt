package com.example.assignment

import com.example.assignment.entity.FqName

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