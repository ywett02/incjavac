package com.example.assignment.storage

import com.example.assignment.entity.Graph
import com.example.assignment.entity.Node
import com.example.assignment.entity.serializer.GraphSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.io.File

abstract class DataStorageGraph<T>(
    private val storageFile: File,
    valueSerializer: KSerializer<T>
) : Closeable {

    private val serializer = GraphSerializer(valueSerializer)

    private val inMemoryData = Graph<T>()
    private val removedKeys = mutableSetOf<T>()

    private val storedData: Graph<T> by lazy {
        val jsonString = getStorageFileOrCreateNew().readText()

        if (jsonString.isEmpty()) {
            Graph<T>()
        } else {
            Json.decodeFromString(serializer, jsonString)
        }
    }

    fun addEdges(source: T, destinations: Set<T>) {
        inMemoryData.addEdge(source, destinations)
        removedKeys.remove(source)
    }

    fun getNode(value: T): Node<T>? {
        return when (value) {
            in inMemoryData -> inMemoryData[value]
            in removedKeys -> null
            in storedData -> storedData[value]
            else -> null
        }
    }

    fun getNodeAndRemove(value: T): Node<T>? {
        val node = getNode(value)
        removeNode(value)
        return node
    }

    fun removeNode(value: T) {
        when (value) {
            in inMemoryData -> inMemoryData.remove(value)
            in storedData -> removedKeys.add(value)
        }
    }

    override fun close() {
        val data = storedData + inMemoryData - removedKeys
        getStorageFileOrCreateNew().writeText(Json.Default.encodeToString(serializer, data))
    }

    private fun getStorageFileOrCreateNew(): File {
        if (!storageFile.exists()) {
            storageFile.parentFile?.mkdirs()
            storageFile.createNewFile()
        }

        return storageFile
    }
}