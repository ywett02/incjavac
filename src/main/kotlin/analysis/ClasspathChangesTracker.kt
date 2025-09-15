package com.example.assignment.analysis

import com.example.assignment.storage.inMemory.ClasspathDigestInMemoryStorage
import com.example.assignment.util.md5
import java.io.File

class ClasspathChangesTracker(
    private val classpathDigestInMemoryStorage: ClasspathDigestInMemoryStorage
) {

    fun hasClasspathChanged(classpath: String?): Boolean {
        val classpathItems = classpath?.split(File.pathSeparator)?.map { File(it) }?.toSet() ?: emptySet()
        return hasClasspathChanged(classpathItems)
    }

    private fun hasClasspathChanged(classpathItems: Set<File>): Boolean {
        val currentMetadata = classpathItems
            .flatMap { item ->
                when {
                    item.isFile -> setOf(item)
                    item.isDirectory -> findClassFiles(item)
                    else -> {
                        throw IllegalArgumentException(
                            "Invalid classpath entry: ${item.absolutePath}. " +
                                    "Expected a JAR/ZIP file or a directory."
                        )
                    }
                }
            }.map { file ->
                file.absoluteFile
            }.associateWith { item -> item.md5 }
        val previousMetadata = classpathDigestInMemoryStorage.getAllAndRemove()
        classpathDigestInMemoryStorage.putAll(currentMetadata)

        return currentMetadata != previousMetadata
    }

    private fun findClassFiles(dir: File): Set<File> {
        return dir.walk().filter { file -> file.name.endsWith(".class") }.toSet()
    }
}