package com.example.assignment

import com.example.assignment.entity.ExitCode
import java.io.File
import javax.tools.JavaCompiler
import javax.tools.StandardJavaFileManager

data class IncrementalJavaCompilerContext(
    val src: File,
    val outputDir: File,
    val classpath: String?,
    val javaCompiler: JavaCompiler,
    val onCompilationCompleted: (ExitCode) -> Unit = {},
) {
    val sourceFiles = findJavaFiles(src)

    val javaFileManager: StandardJavaFileManager = javaCompiler.getStandardFileManager(null, null, null)

    private fun findJavaFiles(src: File): Set<File> {
        return src.walk().filter { file -> file.name.endsWith(".java") }.toSet()
    }
}
