package com.example.javac.incremental.storage.inMemory

import com.example.javac.incremental.entity.FqName
import com.example.javac.incremental.entity.serializer.FileAsAbsPathSerializer
import com.example.javac.incremental.entity.serializer.FqNameAsStringSerializer
import com.example.javac.incremental.storage.DataStorageMap
import com.example.javac.incremental.util.mapOfSetsSerializer
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