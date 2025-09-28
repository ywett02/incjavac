package com.example.javac.incremental.reporter

object StdoutReporter : EventReporter {
    override fun reportEvent(message: String) {
        println(message)
    }
}