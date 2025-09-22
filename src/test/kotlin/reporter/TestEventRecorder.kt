package com.example.assignment.reporter

class TestEventRecorder : EventReporter {

    private val _events = mutableListOf<String>()
    val events: List<String>
        get() = this._events

    override fun reportEvent(message: String) {
        _events.add(message)
    }

    fun clear() {
        _events.clear()
    }
}