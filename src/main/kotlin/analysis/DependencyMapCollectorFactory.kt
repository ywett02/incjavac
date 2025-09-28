package com.example.javac.incremental.analysis

import com.example.javac.incremental.IncrementalJavaCompilerContext
import com.example.javac.incremental.storage.inMemory.DependencyGraphInMemoryStorage
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