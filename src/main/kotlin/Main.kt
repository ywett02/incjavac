package com.example.assignment

fun main(array: Array<String>) {
    val resultCode = IncrementalJavaCompilerCommand.run(array)
    println("Compilation result code: $resultCode")
}