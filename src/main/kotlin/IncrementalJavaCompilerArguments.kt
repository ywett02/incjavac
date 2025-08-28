package com.example.assignment

import java.io.File

//TODO: Do I really need this?
data class IncrementalJavaCompilerArguments(
    val src: File,
    val cacheDir: File,
    val classpath: String?,
    val directory: File?,
) {

    val sourceFiles = findJavaFiles(src)

    private fun findJavaFiles(src: File): List<File> {
        if (src.isDirectory.not()) {
            throw IllegalArgumentException("Provided path is not a directory: ${src.path}")
        }

        return src.walk().filter { file -> file.name.endsWith(".java") }.toList()
    }
}

fun IncrementalJavaCompilerArguments.toJavaCompilerArguments(): List<String> =
    buildList<String> {
        if (classpath != null) {
            add("-cp")
            add(classpath)
        }

        if (directory != null) {
            add("-d")
            add(directory.absolutePath)
        }

        addAll(
            sourceFiles.map { file -> file.absolutePath }
        )
    }
