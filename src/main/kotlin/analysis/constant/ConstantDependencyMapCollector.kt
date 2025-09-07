package com.example.assignment.analysis.constant

import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.sun.source.util.*


class ConstantDependencyMapCollector(
    task: JavacTask,
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage
) : TaskListener {

    private val scanner = ConstantDependencyScanner(task.elements, Trees.instance(task))

    override fun finished(e: TaskEvent) {
        if (e.kind != TaskEvent.Kind.ANALYZE) {
            return
        }

        val treePath = TreePath(e.compilationUnit)
        val context = scanner.scan(treePath, null)

        for ((fqName, dependencies) in context.dependencyMap) {
            dependencyMapInMemoryStorage.append(fqName, dependencies)
        }
    }
}
