package com.example.javac.incremental.storage.inMemory

import com.example.javac.incremental.entity.FqName
import com.example.javac.incremental.entity.serializer.FqNameAsStringSerializer
import com.example.javac.incremental.storage.DataStorageGraph
import java.io.File

class DependencyGraphInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageGraph<FqName>(
    storageFile,
    FqNameAsStringSerializer
) {
    companion object {
        private const val STORAGE_FILE_NAME = "dependencyGraph.json"

        fun create(cacheDir: File): DependencyGraphInMemoryStorage {
            return DependencyGraphInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
        }
    }
}