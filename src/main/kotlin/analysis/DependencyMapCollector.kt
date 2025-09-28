package com.example.javac.incremental.analysis

import com.example.javac.incremental.IncrementalJavaCompilerContext
import com.example.javac.incremental.entity.FqName
import com.example.javac.incremental.storage.inMemory.DependencyGraphInMemoryStorage
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyAnalysis
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File
import javax.lang.model.util.Elements
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class DependencyMapCollector(
    private val elements: Elements,
    private val incrementalJavaCompilerContext: IncrementalJavaCompilerContext,
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) : TaskListener {
    private val visitor: DependencyVisitor = DependencyVisitor()

    override fun finished(e: TaskEvent) {
        if (e.kind != TaskEvent.Kind.GENERATE) {
            return
        }

        val javaFileObject = incrementalJavaCompilerContext.javaFileManager.getJavaFileForOutput(
            StandardLocation.CLASS_OUTPUT,
            elements.getBinaryName(e.typeElement).toString(),
            JavaFileObject.Kind.CLASS,
            null
        )

        collectDependencies(javaFileObject)

        for ((fqName, analysis) in visitor.analysis) {
            dependencyGraphInMemoryStorage.addEdges(fqName, analysis.types)

            val allSuperTypes = getAllSupertypesOf(fqName, visitor.analysis)
            dependencyGraphInMemoryStorage.addEdges(fqName, allSuperTypes)
        }
    }

    private fun collectDependencies(javaFileObject: JavaFileObject) {
        File(javaFileObject.toUri()).inputStream().use { inputStream ->
            ClassReader(inputStream).accept(visitor, 0)
        }
    }

    fun getAllSupertypesOf(
        start: FqName,
        analysis: Map<FqName, DependencyAnalysis>
    ): Set<FqName> {
        val result = mutableSetOf<FqName>()
        val seen = mutableSetOf<FqName>()

        val stack = ArrayDeque<FqName>().apply {
            for (superType in analysis.getValue(start).superTypes) {
                addLast(superType)
            }
        }

        while (stack.isNotEmpty()) {
            val currentSuperType = stack.removeLast()
            if (!seen.add(currentSuperType)) continue

            result.add(currentSuperType)

            analysis[currentSuperType]?.superTypes?.forEach { superType ->
                if (superType !in seen) {
                    stack.addLast(superType)
                }
            }
        }

        return result
    }
}