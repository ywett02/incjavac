package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.entity.DirtyFiles
import com.example.assignment.entity.FileChanges
import com.example.assignment.entity.FqName
import com.example.assignment.entity.Node
import com.example.assignment.storage.DependencyGraphInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.storage.FqnToFileMapInMemoryStorage
import com.example.assignment.util.inverted
import java.io.File
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class DirtyFilesCalculator(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val fqnToFileMapInMemoryStorage: FqnToFileMapInMemoryStorage,
    private val dependencyGraphInMemoryStorage: DependencyGraphInMemoryStorage
) {
    fun calculateDirtyFiles(
        changes: FileChanges,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): DirtyFiles {
        val sourceFiles = changes.addedAndModifiedFiles + changes.removedFiles

        val sourceFilesToFqn = sourceFiles
            .flatMap { file ->
                fileToFqnMapInMemoryStorage.getAndRemove(file) ?: emptySet()
            }.toSet()

        val dependents = sourceFilesToFqn.flatMap { fqn ->
            dependencyGraphInMemoryStorage.getNodeAndRemove(fqn)?.parents?.map { it.value } ?: emptySet()
        }.toSet()

        val dirtyFqn = sourceFilesToFqn + dependents
        val dirtySourceFiles: Set<File> = dirtyFqn.mapNotNull { fqn -> fqnToFileMapInMemoryStorage.getAndRemove(fqn) }.toSet()
        val dirtyClassFiles = dirtyFqn.map { fqn ->
            incrementalJavaCompilerContext.javaFileManager.getJavaFileForOutput(
                StandardLocation.CLASS_OUTPUT,
                fqn.id,
                JavaFileObject.Kind.CLASS,
                null
            )
        }

        return DirtyFiles(
            dirtySourceFiles = dirtySourceFiles,
            dirtyClassFiles = dirtyClassFiles
        )
    }
}