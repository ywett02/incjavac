package com.example.assignment.storage.inMemory

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.storage.DataStorageGraph
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