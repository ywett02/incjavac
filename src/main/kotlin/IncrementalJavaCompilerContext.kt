package com.example.assignment

import java.io.File

data class IncrementalJavaCompilerContext(
    val src: File,
    val directory: File?,
    val classpath: String?,
) {

    val sourceFiles = findJavaFiles(src)

    private fun findJavaFiles(src: File): List<File> {
        if (src.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${src.path}")
        }

        return src.walk().filter { file -> file.name.endsWith(".java") }.toList()
    }
}

fun IncrementalJavaCompilerContext.toJavaCompilerArguments(): List<String> =
    buildList<String> {
        if (classpath != null) {
            add("-cp")
            add(classpath)
        }

        if (directory != null) {
            add("-d")
            add(directory.absolutePath)
        }
    }
