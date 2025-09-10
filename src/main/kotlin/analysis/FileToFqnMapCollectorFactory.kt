package com.example.assignment.analysis

import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.storage.FqnToFileMapInMemoryStorage
import javax.lang.model.util.Elements

class FileToFqnMapCollectorFactory(
    private val fqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val fileToFqnMapInMemoryStorage: FqnToFileMapInMemoryStorage
) {

    fun create(elements: Elements): FileToFqnMapCollector =
        FileToFqnMapCollector(elements, fqnMapInMemoryStorage, fileToFqnMapInMemoryStorage)

}