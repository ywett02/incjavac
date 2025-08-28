package com.example.assignment

import kotlinx.serialization.json.Json
import java.io.File

class FileMetadataStore private constructor(
    private val storageFile: File
) {

    fun store(digests: Map<File, String>) {
        val jsonString = Json.encodeToString(digests.mapKeys { (file, _) -> file.absolutePath })

        storageFile.writeText(jsonString)
    }

    fun read(): Map<File, String> {
        val jsonString = storageFile.readText()
        if (jsonString.isEmpty()) {
            return mapOf()
        }

        return Json.decodeFromString<Map<String, String>>(jsonString)
            .mapKeys { (absolutePath, _) -> File(absolutePath) }
    }

    companion object {
        private const val STORAGE_FILE_NAME = "fileMetadata.json"

        fun create(cacheDir: File): FileMetadataStore {
            val storageFile = cacheDir.resolve(STORAGE_FILE_NAME)
            if (storageFile.exists().not()) {
                storageFile.parentFile?.mkdirs()
                storageFile.createNewFile()
            }

            return FileMetadataStore(storageFile)
        }
    }
}