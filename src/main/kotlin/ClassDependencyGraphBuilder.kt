package com.example.assignment

import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File

class ClassDependencyGraphBuilder {

    private val visitor: DependencyVisitor = DependencyVisitor()

    fun buildGraph(outDir: File): Map<String, Set<String>> {
        if (outDir.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${outDir.path}")
        }

        return collectDependencies(outDir).inverted()
    }

    private fun collectDependencies(outDir: File): Map<String, Set<String>> {
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

    private fun Map<String, Set<String>>.inverted(): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()

        for ((root, deps) in this) {
            for (dep in deps) {
                result.computeIfAbsent(dep) { mutableSetOf() }.add(root)
            }
        }

        return result
    }
}