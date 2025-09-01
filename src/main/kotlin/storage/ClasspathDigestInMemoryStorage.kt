package com.example.assignment.storage

import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.util.mapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.Closeable
import java.io.File

class ClasspathDigestInMemoryStorage private constructor(
    private val dataStorage: DataStorage<Map<File, String>>
) : Closeable {

    private val inMemoryData: MutableMap<File, String> by lazy {
        dataStorage.load()?.toMutableMap() ?: mutableMapOf()
    }

    fun set(data: Map<File, String>) {
        inMemoryData.clear()
        inMemoryData.putAll(data)
    }

    fun get(): Map<File, String> {
        return inMemoryData
    }

    override fun close() {
        dataStorage.save(inMemoryData)
        inMemoryData.clear()
    }

    companion object {
        private const val STORAGE_FILE_NAME = "classpathDigest.json"

        fun create(cacheDir: File): ClasspathDigestInMemoryStorage {
            val dataStorage = DataStorage<Map<File, String>>(
                cacheDir.resolve(STORAGE_FILE_NAME),
                mapSerializer(FileAsAbsPathSerializer, String.serializer())
            )

            return ClasspathDigestInMemoryStorage(dataStorage)
        }
    }
}