package com.example.assignment.storage.inMemory

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.storage.DataStorageMap
import com.example.assignment.util.mapOfSetsSerializer
import java.io.File

class FileToFqnMapInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<File, Set<FqName>>(
    storageFile,
    mapOfSetsSerializer(FileAsAbsPathSerializer, FqNameAsStringSerializer)
) {

    fun append(key: File, value: Set<FqName>) {
        val data = get(key) ?: return put(key, value)
        put(key, data.plus(value))
    }

    companion object {
        private const val STORAGE_FILE_NAME = "fileToFqn.json"

        fun create(cacheDir: File): FileToFqnMapInMemoryStorage {
            return FileToFqnMapInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
        }
    }
}