package com.example.assignment

import kotlinx.serialization.json.Json
import java.io.File

class ClassDependencyGraphStore private constructor(
    private val storageFile: File
) {

    fun store(graph: Map<String, Set<String>>) {
        val jsonString = Json.encodeToString(graph)

        storageFile.writeText(jsonString)
    }

    fun read(): Map<String, Set<String>> {
        val jsonString = storageFile.readText()
        if (jsonString.isEmpty()) {
            return mapOf()
        }

        return Json.decodeFromString<Map<String, Set<String>>>(jsonString)
    }

    companion object {
        private const val STORAGE_FILE_NAME = "depGraph.json"

        fun create(cacheDir: File): ClassDependencyGraphStore {
            val storageFile = cacheDir.resolve(STORAGE_FILE_NAME)
            if (storageFile.exists().not()) {
                storageFile.parentFile?.mkdirs()
                storageFile.createNewFile()
            }

            return ClassDependencyGraphStore(storageFile)
        }
    }
}