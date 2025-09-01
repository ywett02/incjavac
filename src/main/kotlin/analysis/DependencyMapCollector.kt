package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.storage.DependencyMapInMemoryStorage
import org.objectweb.asm.ClassReader
import org.objectweb.asm.depend.DependencyVisitor
import java.io.File
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class DependencyMapCollector(
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage
) {

    private val visitor: DependencyVisitor = DependencyVisitor()

    fun collectDependencies(incrementalJavaCompilerContext: IncrementalJavaCompilerContext) {
        incrementalJavaCompilerContext.javaFileManager.list(
            StandardLocation.CLASS_OUTPUT,
            "",
            setOf(JavaFileObject.Kind.CLASS),
            true
        )
            .map { javaObject ->
                File(javaObject.toUri())
            }.forEach { file ->
                file.inputStream().use { inputStream ->
                    ClassReader(inputStream).accept(visitor, 0)
                }
            }

        dependencyMapInMemoryStorage.set(visitor.globals)
    }
}