package com.example.assignment.storage.inMemory

import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.storage.DataStorageMap
import com.example.assignment.util.mapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class ClasspathDigestInMemoryStorage private constructor(
    storageFile: File,
) : DataStorageMap<File, String>(storageFile, mapSerializer(FileAsAbsPathSerializer, String.Companion.serializer())) {

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