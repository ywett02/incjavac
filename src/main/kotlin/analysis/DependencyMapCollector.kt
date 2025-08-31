package com.example.assignment.collector

import com.example.assignment.entity.FqName
import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File

class DependencyMapCollector {

    private val visitor: DependencyVisitor = DependencyVisitor()

    fun collectDependencies(file: File): Map<FqName, Set<FqName>> {
        file.inputStream().use { inputStream ->
            ClassReader(inputStream).accept(visitor, 0)
        }

        return visitor.globals
    }
}