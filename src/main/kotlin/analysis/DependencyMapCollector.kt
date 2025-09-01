package com.example.assignment.analysis

import com.example.assignment.EventReporter
import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.example.assignment.util.joinToString
import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File

class DependencyMapCollector(
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage,
    private val eventReporter: EventReporter
) {

    private val visitor: DependencyVisitor = DependencyVisitor()

    fun collectDependencies(outDir: File) {
        if (outDir.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${outDir.path}")
        }

        findClassFiles(outDir).forEach { file ->
            file.inputStream().use { inputStream ->
                ClassReader(inputStream).accept(visitor, 0)
            }
        }

        eventReporter.reportEvent(
            """Dependency graph created: [
                |${visitor.globals.joinToString({ it.id }, { it.id })}]""".trimMargin()
        )

        dependencyMapInMemoryStorage.set(visitor.globals)
    }

    private fun findClassFiles(dir: File): List<File> {
        return dir.walk().filter { file -> file.name.endsWith(".class") }.toList()
    }
}