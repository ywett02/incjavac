package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.storage.DependencyMapInMemoryStorage
import javax.lang.model.util.Elements

class DependencyMapCollectorFactory(
    private val fqnMapInMemoryStorage: DependencyMapInMemoryStorage
) {

    fun create(
        elements: Elements,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): DependencyMapCollector =
        DependencyMapCollector(elements, incrementalJavaCompilerContext, fqnMapInMemoryStorage)

}