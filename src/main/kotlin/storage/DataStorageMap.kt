package com.example.assignment.storage

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import java.io.Closeable
import java.io.File

abstract class DataStorageMap<K, V>(
    private val storageFile: File,
    private val serializer: KSerializer<Map<K, V>>
) : Storage {

    private val inMemoryData = mutableMapOf<K, V>()
    private val removedKeys = mutableSetOf<K>()

    private val storedData: Map<K, V> by lazy {
        val jsonString = getStorageFileOrCreateNew().readText()

        if (jsonString.isEmpty()) {
            emptyMap()
        } else {
            Json.decodeFromString<Map<K, V>>(serializer, jsonString)
        }
    }

    fun put(key: K, value: V) {
        inMemoryData[key] = value
        removedKeys.remove(key)
    }

    fun putAll(from: Map<out K, V>) {
        inMemoryData.putAll(from)
        removedKeys.removeAll(from.keys)
    }

    fun get(key: K): V? {
        return when (key) {
            in inMemoryData -> inMemoryData[key]
            in removedKeys -> null
            in storedData -> storedData[key]
            else -> null
        }
    }

    fun getAndRemove(key: K): V? {
        val data = get(key)
        remove(key)
        return data
    }

    fun getAll(): Map<K, V> {
        return storedData + inMemoryData - removedKeys
    }

    fun remove(key: K) {
        when (key) {
            in inMemoryData -> inMemoryData.remove(key)
            in storedData -> removedKeys.add(key)
        }
    }

    fun removeAll() {
        inMemoryData.clear()
        removedKeys.addAll(storedData.keys)
    }

    override fun flush() {
        val data = storedData + inMemoryData - removedKeys
        getStorageFileOrCreateNew().writeText(Json.encodeToString(serializer, data))
    }

    private fun getStorageFileOrCreateNew(): File {
        if (!storageFile.exists()) {
            storageFile.parentFile?.mkdirs()
            storageFile.createNewFile()
        }

        return storageFile
    }
}