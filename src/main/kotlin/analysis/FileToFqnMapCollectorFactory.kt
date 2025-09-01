package com.example.assignment.analysis

import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import javax.lang.model.util.Elements

class FileToFqnMapCollectorFactory(
    private val fqnMapInMemoryStorage: FileToFqnMapInMemoryStorage
) {

    fun create(elements: Elements): FileToFqnMapCollector =
        FileToFqnMapCollector(elements, fqnMapInMemoryStorage)

}