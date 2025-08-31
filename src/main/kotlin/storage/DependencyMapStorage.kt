package com.example.assignment.storage

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.util.mapOfSetsSerializer
import java.io.File

class DependencyMapStorage constructor(
    private val dataStorage: DataStorage<Map<FqName, Set<FqName>>>
) {

    fun save(data: Map<FqName, Set<FqName>>) {
        dataStorage.save(data)
    }

    fun load(): Map<FqName, Set<FqName>>? {
        return dataStorage.load()
    }

    companion object {
        private const val STORAGE_FILE_NAME = "dependencyMap.json"

        fun create(cacheDir: File): DependencyMapStorage {
            val dataStorage = DataStorage<Map<FqName, Set<FqName>>>(
                cacheDir.resolve(STORAGE_FILE_NAME),
                mapOfSetsSerializer(FqNameAsStringSerializer, FqNameAsStringSerializer)
            )

            return DependencyMapStorage(dataStorage)
        }
    }
}