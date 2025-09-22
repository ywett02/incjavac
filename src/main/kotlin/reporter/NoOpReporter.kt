package com.example.assignment.reporter

object NoOpReporter : EventReporter {
    override fun reportEvent(message: String) {
        //no-op
    }
}