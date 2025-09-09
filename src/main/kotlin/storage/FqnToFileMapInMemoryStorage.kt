package com.example.assignment.storage

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.util.mapOfSetsSerializer
import com.example.assignment.util.mapSerializer
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