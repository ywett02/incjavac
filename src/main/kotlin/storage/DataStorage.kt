package com.example.assignment.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.File

class DataStorage<T>(
    private val storageFile: File,
    private val serializer: KSerializer<T>
) {

    fun save(data: T) {
        if (!storageFile.exists()) {
            storageFile.parentFile?.mkdirs()
            storageFile.createNewFile()
        }

        storageFile.writeText(Json.encodeToString(serializer, data))
    }

    fun load(): T? {
        if (!storageFile.exists()) {
            return null
        }

        val jsonString = storageFile.readText()
        return Json.decodeFromString<T>(serializer, jsonString)
    }

    fun exists(): Boolean =
        storageFile.exists()
}