package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.storage.inMemory.DependencyGraphInMemoryStorage
import javax.lang.model.util.Elements

class DependencyMapCollectorFactory(
    private val fqnMapInMemoryStorage: DependencyGraphInMemoryStorage
) {

    fun create(
        elements: Elements,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): DependencyMapCollector =
        DependencyMapCollector(elements, incrementalJavaCompilerContext, fqnMapInMemoryStorage)

}