package com.example.assignment

import java.io.File

data class IncrementalJavaCompilerContext(
    val src: File,
    val directory: File,
    val cacheDir: File,
    val classpath: String?,
) {

    val sourceFiles = findJavaFiles(src)
    val classFiles
        get() = findClassFiles(directory)

    private fun findJavaFiles(src: File): Set<File> {
        if (src.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${src.path}")
        }

        return src.walk().filter { file -> file.name.endsWith(".java") }.toSet()
    }

    private fun findClassFiles(src: File): Set<File> {
        if (src.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${src.path}")
        }

        return src.walk().filter { file -> file.name.endsWith(".class") }.toSet()
    }
}
