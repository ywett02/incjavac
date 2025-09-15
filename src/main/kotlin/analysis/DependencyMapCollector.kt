package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.storage.inMemory.DependencyGraphInMemoryStorage
import com.sun.source.util.TaskEvent
import com.sun.source.util.TaskListener
import org.objectweb.asm.ClassReader
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

        for ((fqName, dependencies) in visitor.globals) {
            dependencyGraphInMemoryStorage.addEdges(fqName, dependencies)
        }
    }

    private fun collectDependencies(javaFileObject: JavaFileObject) {
        File(javaFileObject.toUri()).inputStream().use { inputStream ->
            ClassReader(inputStream).accept(visitor, 0)
        }
    }
}