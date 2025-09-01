package com.example.assignment

import java.io.File
import javax.tools.JavaCompiler
import javax.tools.StandardJavaFileManager
import javax.tools.StandardLocation

data class IncrementalJavaCompilerContext(
    val src: File,
    val outputDir: File,
    val metadataDir: File,
    val classpath: String?,
    val javaCompiler: JavaCompiler
) {

    init {
        if (outputDir.exists().not()) {
            outputDir.mkdirs()
        }
    }

    val sourceFiles = findJavaFiles(src)

    val javaFileManager: StandardJavaFileManager = javaCompiler.getStandardFileManager(null, null, null).apply {
        setLocation(StandardLocation.CLASS_OUTPUT, setOf(outputDir))
    }

    private fun findJavaFiles(src: File): Set<File> {
        return src.walk().filter { file -> file.name.endsWith(".java") }.toSet()
    }
}
