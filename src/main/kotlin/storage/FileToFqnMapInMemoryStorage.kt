package com.example.assignment.storage

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FileAsAbsPathSerializer
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.util.mapOfSetsSerializer
import java.io.Closeable
import java.io.File

class FileToFqnMapInMemoryStorage private constructor(
    private val dataStorage: DataStorage<Map<File, Set<FqName>>>
) : Closeable {

    private val inMemoryData: MutableMap<File, MutableSet<FqName>> by lazy {
        dataStorage.load()?.mapValues { (_, set) -> set.toMutableSet() }?.toMutableMap() ?: mutableMapOf()
    }

    fun set(data: Map<File, Set<FqName>>) {
        for ((key, value) in data) {
            inMemoryData.computeIfAbsent(key) { mutableSetOf() }.addAll(value)
        }
    }

    fun get(): Map<File, Set<FqName>> {
        return inMemoryData
    }

    fun remove(key: File) {
        inMemoryData.remove(key)
    }

    fun exists() = dataStorage.exists()

    override fun close() {
        dataStorage.save(inMemoryData)
        inMemoryData.clear()
    }

    companion object {
        private const val STORAGE_FILE_NAME = "fileToFqn.json"

        fun create(cacheDir: File): FileToFqnMapInMemoryStorage {
            val dataStorage = DataStorage<Map<File, Set<FqName>>>(
                cacheDir.resolve(STORAGE_FILE_NAME),
                mapOfSetsSerializer(FileAsAbsPathSerializer, FqNameAsStringSerializer)
            )

            return FileToFqnMapInMemoryStorage(dataStorage)
        }
    }
}