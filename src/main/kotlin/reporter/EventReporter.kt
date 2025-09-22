package com.example.assignment.reporter

interface EventReporter {
    fun reportEvent(message: String)
}

fun EventReporter(enabled: Boolean): EventReporter =
    if (enabled) {
        StdoutReporter
    } else {
        NoOpReporter
    }
