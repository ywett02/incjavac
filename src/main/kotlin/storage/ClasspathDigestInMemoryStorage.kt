package com.example.assignment.storage

import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.util.mapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class ClasspathDigestInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<File, String>(storageFile, mapSerializer(FileAsAbsPathSerializer, String.serializer())) {

    companion object {
        private const val STORAGE_FILE_NAME = "classpathDigest.json"

        fun create(cacheDir: File): ClasspathDigestInMemoryStorage {
            return ClasspathDigestInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
        }
    }
}