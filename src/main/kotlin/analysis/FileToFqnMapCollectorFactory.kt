package com.example.javac.incremental.analysis

import com.example.javac.incremental.storage.inMemory.FileToFqnMapInMemoryStorage
import com.example.javac.incremental.storage.inMemory.FqnToFileMapInMemoryStorage
import javax.lang.model.util.Elements

class FileToFqnMapCollectorFactory(
    private val fqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val fileToFqnMapInMemoryStorage: FqnToFileMapInMemoryStorage
) {

    fun create(elements: Elements): FileToFqnMapCollector =
        FileToFqnMapCollector(elements, fqnMapInMemoryStorage, fileToFqnMapInMemoryStorage)

}