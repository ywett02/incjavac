package com.example.assignment.analysis

import com.example.assignment.entity.FileChanges
import com.example.assignment.entity.FqName
import com.example.assignment.storage.DependencyGraphInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.util.inverted
import java.io.File

class DirtyFilesCalculator(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) {
    fun calculateDirtyFiles(changes: FileChanges): Set<File> {

        val fileToFqnMap: Map<File, Set<FqName>> = fileToFqnMapInMemoryStorage.getAll()
        val fqnToFileMap: Map<FqName, Set<File>> = fileToFqnMap.inverted()

        val sourceFiles = changes.addedAndModifiedFiles + changes.removedFiles
        return sourceFiles
            .asSequence()
            .flatMap { sourceFile: File ->
                fileToFqnMap.getOrDefault(sourceFile, emptySet())
            }
            .flatMap { classFqnName ->
               dependencyGraphInMemoryStorage.getNodeAndRemove(classFqnName)?.parents?.map { it.value } ?: emptySet()
            }
            .flatMap { dependencyFqnName ->
                fqnToFileMap.getOrDefault(dependencyFqnName, emptySet())
            }
            .plus(changes.addedAndModifiedFiles)
            .minus(changes.removedFiles)
            .toSet()
    }
}