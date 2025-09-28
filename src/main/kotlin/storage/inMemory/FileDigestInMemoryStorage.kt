package com.example.javac.incremental.storage.inMemory

import com.example.javac.incremental.entity.serializer.FileAsAbsPathSerializer
import com.example.javac.incremental.storage.DataStorageMap
import com.example.javac.incremental.util.mapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class FileDigestInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<File, String>(
    storageFile = storageFile,
    serializer = mapSerializer(FileAsAbsPathSerializer, String.serializer())
) {
    companion object {
        private const val STORAGE_FILE_NAME = "fileDigest.json"

        fun create(cacheDir: File): FileDigestInMemoryStorage =
            FileDigestInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
    }
}