package com.example.javac.incremental.storage.inMemory

import com.example.javac.incremental.entity.serializer.FileAsAbsPathSerializer
import com.example.javac.incremental.storage.DataStorageMap
import com.example.javac.incremental.util.mapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class ClasspathDigestInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<File, String>(storageFile, mapSerializer(FileAsAbsPathSerializer, String.serializer())) {

    fun getAllAndRemove(): Map<File, String> {
        val data = getAll()
        removeAll()
        return data
    }

    companion object {
        private const val STORAGE_FILE_NAME = "classpathDigest.json"

        fun create(cacheDir: File): ClasspathDigestInMemoryStorage {
            return ClasspathDigestInMemoryStorage(cacheDir.resolve(STORAGE_FILE_NAME))
        }
    }
}