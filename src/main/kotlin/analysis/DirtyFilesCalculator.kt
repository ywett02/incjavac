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
        //TODO this will not work for file addition
        val sourceFiles = changes.addedAndModifiedFiles + changes.removedFiles

        val sourceFilesToFqn = sourceFiles
            .flatMap { file ->
                fileToFqnMapInMemoryStorage.getAndRemove(file) ?: emptySet()
            }.toSet()

        val dependents = sourceFilesToFqn.flatMap { fqn ->
            dependencyGraphInMemoryStorage.getNodeAndRemove(fqn)?.parents?.map { it.value } ?: emptySet()
        }.toSet()

        val dirtyFqn = sourceFilesToFqn + dependents

        val dirtySourceFiles: Set<File> =
            dirtyFqn.mapNotNull { fqn -> fqnToFileMapInMemoryStorage.getAndRemove(fqn) }.toSet()

        val dirtyClassFiles = dirtyFqn.map { fqn ->
            val relativePath = fqn.id.split(".").joinToString(File.separator)
            incrementalJavaCompilerContext.outputDir.resolve(relativePath).absoluteFile
        }.toSet()

        return DirtyFiles(
            dirtySourceFiles = dirtySourceFiles,
            dirtyClassFiles = dirtyClassFiles
        )
    }

//    fun calculateDirtyFiles(changes: FileChanges): Set<File> {
//        val fileToFqnMap: Map<File, Set<FqName>> = fileToFqnMapInMemoryStorage.getAll()
//        val invertedDependencyMap: Map<FqName, Set<FqName>> = dependencyMapInMemoryStorage.getAll().inverted()
//        val fqnToFileMap: Map<FqName, Set<File>> = fileToFqnMap.inverted()
//
//        val sourceFiles = changes.addedAndModifiedFiles + changes.removedFiles
//        return sourceFiles
//            .asSequence()
//            .flatMap { sourceFile: File ->
//                fileToFqnMap.getOrDefault(sourceFile, emptySet())
//            }
//            .flatMap { classFqnName ->
//                invertedDependencyMap.getOrDefault(classFqnName, emptySet())
//            }
//            .flatMap { dependencyFqnName ->
//                fqnToFileMap.getOrDefault(dependencyFqnName, emptySet())
//            }
//            .plus(changes.addedAndModifiedFiles)
//            .minus(changes.removedFiles)
//            .toSet()
//    }
}