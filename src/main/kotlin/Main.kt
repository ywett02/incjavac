package com.example.assignment

fun main(array: Array<String>) {
    val exitCode = IncrementalJavaCompilerCommand.run(array)
    println("Compilation result code: ${exitCode.code}")
}