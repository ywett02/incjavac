package com.example.assignment.util

import com.example.assignment.entity.FqName
import java.io.File

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