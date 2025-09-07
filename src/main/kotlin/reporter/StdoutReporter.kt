package com.example.assignment.reporter

object StdoutReporter : EventReporter {
    override fun reportEvent(message: String) {
        println(message)
    }
}