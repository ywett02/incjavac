package com.example.javac.incremental.reporter

object NoOpReporter : EventReporter {
    override fun reportEvent(message: String) {
        //no-op
    }
}