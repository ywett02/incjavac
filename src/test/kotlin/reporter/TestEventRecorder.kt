package com.example.javac.incremental.reporter

class TestEventRecorder : EventReporter {

    private val _events = mutableListOf<String>()
    val events: List<String>
        get() = this._events

    override fun reportEvent(message: String) {
        _events.add(message)
    }
}