package com.example.assignment.analysis

import com.example.assignment.entity.FileChanges
import com.example.assignment.entity.FqName
import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.util.inverted
import java.io.File

class DirtyFilesCalculator(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage
) {
    fun calculateDirtyFiles(changes: FileChanges): Set<File> {
        val fileToFqnMap: Map<File, Set<FqName>> = fileToFqnMapInMemoryStorage.get()
        val invertedDependencyMap: Map<FqName, Set<FqName>> = dependencyMapInMemoryStorage.get().inverted()
        val fqnToFileMap: Map<FqName, Set<File>> = fileToFqnMap.inverted()

        val sourceFiles = changes.addedAndModifiedFiles + changes.removedFiles
        return sourceFiles
            .asSequence()
            .flatMap { sourceFile: File ->
                fileToFqnMap.getOrDefault(sourceFile, emptySet())
            }
            .flatMap { classFqnName ->
                invertedDependencyMap.getOrDefault(classFqnName, emptySet())
            }
            .flatMap { dependencyFqnName ->
                fqnToFileMap.getOrDefault(dependencyFqnName, emptySet())
            }
            .plus(changes.addedAndModifiedFiles)
            .toSet()
    }
}