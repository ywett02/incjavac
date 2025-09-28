package com.example.javac.incremental.analysis

import com.example.javac.incremental.IncrementalJavaCompilerContext
import com.example.javac.incremental.entity.DirtyFiles
import com.example.javac.incremental.entity.FileChanges
import com.example.javac.incremental.storage.inMemory.DependencyGraphInMemoryStorage
import com.example.javac.incremental.storage.inMemory.FileToFqnMapInMemoryStorage
import com.example.javac.incremental.storage.inMemory.FqnToFileMapInMemoryStorage
import java.io.File

class DirtyFilesCalculator(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val fqnToFileMapInMemoryStorage: FqnToFileMapInMemoryStorage,
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) {
    fun calculateDirtyFiles(
        changes: FileChanges,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): DirtyFiles {
        val dirtySourceFiles = mutableSetOf<File>().apply {
            addAll(changes.addedAndModifiedFiles)
            addAll(getDependencies(changes.addedAndModifiedFiles + changes.removedFiles))

            removeAll(changes.removedFiles)
        }

        val dirtyClassFiles = (dirtySourceFiles + changes.removedFiles)
            .flatMap { file ->
                fileToFqnMapInMemoryStorage.getAndRemove(file) ?: emptySet()
            }.map { fqn ->
                val relativePath = fqn.id.split(".").joinToString(File.separator)
                incrementalJavaCompilerContext.outputDir.resolve("$relativePath.class").absoluteFile
            }.toSet()

        return DirtyFiles(
            dirtySourceFiles = dirtySourceFiles,
            dirtyClassFiles = dirtyClassFiles
        )
    }

    private fun getDependencies(dirtySourceFiles: Set<File>): Set<File> =
        dirtySourceFiles
            .flatMap { file ->
                fileToFqnMapInMemoryStorage.get(file) ?: emptySet()
            }.flatMap { fqn ->
                dependencyGraphInMemoryStorage.getNodeAndRemove(fqn)?.parents?.map { it.value } ?: emptySet()
            }.mapNotNull { fqn ->
                fqnToFileMapInMemoryStorage.getAndRemove(fqn)
            }.toSet()
}