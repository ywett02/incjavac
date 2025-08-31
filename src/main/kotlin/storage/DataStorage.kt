package com.example.assignment.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

class DataStorage<T>(
    private val storageFile: File,
    private val serializer: KSerializer<T>
) {

    private var inMemoryData: T? = null

    fun save(data: T) {
        if (!storageFile.exists()) {
            storageFile.parentFile?.mkdirs()
            storageFile.createNewFile()
        }

        storageFile.writeText(Json.encodeToString(serializer, data))
    }

    fun load(): T? {
        if (!storageFile.exists()) {
            inMemoryData = null
            return null
        }

        if (inMemoryData != null) {
            return inMemoryData
        }

        val jsonString = storageFile.readText()
        if (jsonString.isBlank()) {
            return null
        }

        return Json.decodeFromString<T>(serializer, jsonString)
    }
}