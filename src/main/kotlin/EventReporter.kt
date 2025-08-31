package com.example.assignment

class EventReporter(
    private val enabled: Boolean
) {

    fun reportEvent(message: String) {
        if (enabled) {
            println(message)
        }
    }
}