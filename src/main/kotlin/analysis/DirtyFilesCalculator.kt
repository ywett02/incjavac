package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.entity.DirtyFiles
import com.example.assignment.entity.FileChanges
import com.example.assignment.storage.DependencyGraphInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.storage.FqnToFileMapInMemoryStorage
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

        val dirtyClassFiles = dirtySourceFiles
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