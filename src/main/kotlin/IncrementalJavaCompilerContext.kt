package com.example.javac.incremental

import com.example.javac.incremental.transaction.CompilationTransaction
import java.io.File
import javax.tools.JavaCompiler
import javax.tools.StandardJavaFileManager

data class IncrementalJavaCompilerContext(
    val src: File,
    val outputDir: File,
    val classpath: String?,
    val javaCompiler: JavaCompiler,
    val compilationTransaction: CompilationTransaction,
) {
    val sourceFiles by lazy {
        findJavaFiles(src)
    }

    val javaFileManager: StandardJavaFileManager = javaCompiler.getStandardFileManager(null, null, null)

    private fun findJavaFiles(src: File): Set<File> {
        return src.walk().filter { file -> file.name.endsWith(".java") }.toSet()
    }
}