package com.example.assignment.storage

import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.util.mapSerializer
import kotlinx.serialization.builtins.serializer
import java.io.File

class FileDigestStorage private constructor(
    private val dataStorage: DataStorage<Map<File, String>>
) {

    fun save(data: Map<File, String>) {
        val previousData = load() ?: return dataStorage.save(data)
        val mergedData = previousData.toMutableMap().apply { putAll(data) }

        dataStorage.save(mergedData)
    }

    fun load(): Map<File, String>? {
        return dataStorage.load()
    }

    companion object {
        private const val STORAGE_FILE_NAME = "fileDigest.json"

        fun create(cacheDir: File): FileDigestStorage {
            val dataStorage = DataStorage<Map<File, String>>(
                cacheDir.resolve(STORAGE_FILE_NAME),
                mapSerializer(FileAsAbsPathSerializer, String.serializer())
            )

            return FileDigestStorage(dataStorage)
        }
    }
}