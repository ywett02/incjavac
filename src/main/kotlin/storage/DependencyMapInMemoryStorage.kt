package com.example.assignment.storage

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.util.mapOfSetsSerializer
import java.io.File

class DependencyMapInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<FqName, Set<FqName>>(
    storageFile,
    mapOfSetsSerializer(FqNameAsStringSerializer, FqNameAsStringSerializer)
) {

    fun append(key: FqName, value: Set<FqName>) {
        val data = get(key) ?: return put(key, value)
        put(key, data.plus(value))
    }

    companion object {
        private const val STORAGE_FILE_NAME = "dependencyMap.json"

        fun create(cacheDir: File): DependencyMapInMemoryStorage {
            return DependencyMapInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
        }
    }
}