package com.example.javac.incremental.analysis.constant

import com.example.javac.incremental.storage.inMemory.DependencyGraphInMemoryStorage
import com.sun.source.util.JavacTask


class ConstantDependencyMapCollectorFactory(
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) {

    fun create(
        javacTask: JavacTask,
    ): ConstantDependencyMapCollector =
        ConstantDependencyMapCollector(javacTask, dependencyGraphInMemoryStorage)
}