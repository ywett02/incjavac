package com.example.assignment.analysis

import com.example.assignment.EventReporter
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import javax.lang.model.util.Elements

class FileToFqnMapCollectorFactory(
    private val fqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val eventReporter: EventReporter
) {

    fun create(elements: Elements): FileToFqnMapCollector =
        FileToFqnMapCollector(elements, fqnMapInMemoryStorage, eventReporter)

}