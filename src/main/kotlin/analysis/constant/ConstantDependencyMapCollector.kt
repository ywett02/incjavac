package com.example.javac.incremental.analysis.constant

import com.example.javac.incremental.storage.inMemory.DependencyGraphInMemoryStorage
import com.sun.source.util.*


class ConstantDependencyMapCollector(
    task: JavacTask,
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) : TaskListener {

    private val scanner = ConstantDependencyScanner(task.elements, Trees.instance(task))

    override fun finished(e: TaskEvent) {
        if (e.kind != TaskEvent.Kind.ANALYZE) {
            return
        }

        val treePath = TreePath(e.compilationUnit)
        val context = scanner.scan(treePath, null)

        for ((fqName, dependencies) in context.dependencyMap) {
            dependencyGraphInMemoryStorage.addEdges(fqName, dependencies)
        }
    }
}
