package com.example.assignment.collector

import com.example.assignment.entity.FqName
import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File

class DependencyMapCollector {

    private val visitor: DependencyVisitor = DependencyVisitor()

    fun collectDependencies(outDir: File): Map<FqName, Set<FqName>> {
        if (outDir.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${outDir.path}")
        }

        findClassFiles(outDir).forEach { file ->
            file.inputStream().use { inputStream ->
                ClassReader(inputStream).accept(visitor, 0)
            }
        }

        return visitor.globals
    }

    private fun findClassFiles(dir: File): List<File> {
        return dir.walk().filter { file -> file.name.endsWith(".class") }.toList()
    }
}