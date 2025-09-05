package com.example.assignment.analysis.constant

import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.sun.source.util.JavacTask


class ConstantDependencyMapCollectorFactory(
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage
) {

    fun create(
        javacTask: JavacTask,
    ): ConstantDependencyMapCollector =
        ConstantDependencyMapCollector(javacTask, dependencyMapInMemoryStorage)
}