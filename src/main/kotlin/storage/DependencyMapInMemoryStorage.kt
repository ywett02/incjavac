package com.example.assignment.storage

import com.example.assignment.entity.FqName
import com.example.assignment.entity.serializer.FqNameAsStringSerializer
import com.example.assignment.util.mapOfSetsSerializer
import java.io.Closeable
import java.io.File

class DependencyMapInMemoryStorage constructor(
    private val dataStorage: DataStorage<Map<FqName, Set<FqName>>>
) : Closeable {

    private val inMemoryData: MutableMap<FqName, MutableSet<FqName>> by lazy {
        dataStorage.load()?.mapValues { (_, set) -> set.toMutableSet() }?.toMutableMap() ?: mutableMapOf()
    }

    fun addAll(data: Map<FqName, Set<FqName>>) {
        for ((key, value) in data) {
            inMemoryData.computeIfAbsent(key) { mutableSetOf() }.addAll(value)
        }
    }

    fun get(): Map<FqName, Set<FqName>> {
        return inMemoryData.toMap()
    }

    fun remove(key: FqName) {
        inMemoryData.remove(key)
    }

    override fun close() {
        dataStorage.save(inMemoryData)
        inMemoryData.clear()
    }

    companion object {
        private const val STORAGE_FILE_NAME = "dependencyMap.json"

        fun create(cacheDir: File): DependencyMapInMemoryStorage {
            val dataStorage = DataStorage<Map<FqName, Set<FqName>>>(
                cacheDir.resolve(STORAGE_FILE_NAME),
                mapOfSetsSerializer(FqNameAsStringSerializer, FqNameAsStringSerializer)
            )

            return DependencyMapInMemoryStorage(dataStorage)
        }
    }
}