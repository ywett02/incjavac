package com.example.assignment.storage

import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.util.mapSerializer
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