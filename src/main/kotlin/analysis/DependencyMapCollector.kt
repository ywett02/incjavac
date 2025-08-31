package com.example.assignment.analysis

import com.example.assignment.entity.FqName
import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File

class DependencyMapCollector {

    private val visitor: DependencyVisitor = DependencyVisitor()
    val dependencyMap: Map<FqName, Set<FqName>>
        get() = visitor.globals

    fun collectDependencies(file: File) {
        file.inputStream().use { inputStream ->
            ClassReader(inputStream).accept(visitor, 0)
        }
    }
}