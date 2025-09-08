package com.example.assignment.analysis.constant

import com.example.assignment.storage.DependencyGraphInMemoryStorage
import com.sun.source.util.JavacTask


class ConstantDependencyMapCollectorFactory(
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) {

    fun create(
        javacTask: JavacTask,
    ): ConstantDependencyMapCollector =
        ConstantDependencyMapCollector(javacTask, dependencyGraphInMemoryStorage)
}