package com.example.assignment

import java.io.File
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

data class IncrementalJavaCompilerContext(
    val src: File,
    val directory: File,
    val cacheDir: File,
    val classpath: String?,
    val javaCompiler: JavaCompiler
) {

    init {
        require(src.isDirectory) { "Expected a valid source directory, but got: ${src.absolutePath}" }
        directory.mkdirs()
    }

    val sourceFiles = findJavaFiles(src)

    val javaFileManager = javaCompiler.getStandardFileManager(null, null, null).apply {
        setLocation(StandardLocation.CLASS_OUTPUT, setOf(directory))
    }

    val classObjects
        get() = javaFileManager.list(StandardLocation.CLASS_OUTPUT, "", setOf(JavaFileObject.Kind.CLASS), true)

    private fun findJavaFiles(src: File): Set<File> {
        if (src.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${src.path}")
        }

        return src.walk().filter { file -> file.name.endsWith(".java") }.toSet()
    }
}
