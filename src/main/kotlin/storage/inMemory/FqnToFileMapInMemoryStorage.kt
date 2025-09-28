package com.example.javac.incremental.storage.inMemory

import com.example.javac.incremental.entity.FqName
import com.example.javac.incremental.entity.serializer.FileAsAbsPathSerializer
import com.example.javac.incremental.entity.serializer.FqNameAsStringSerializer
import com.example.javac.incremental.storage.DataStorageMap
import com.example.javac.incremental.util.mapSerializer
import java.io.File

class FqnToFileMapInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<FqName, File>(
    storageFile,
    mapSerializer(FqNameAsStringSerializer, FileAsAbsPathSerializer)
) {
    companion object {
        private const val STORAGE_FILE_NAME = "fqnToFile.json"

        fun create(cacheDir: File): FqnToFileMapInMemoryStorage {
            return FqnToFileMapInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
        }
    }
}