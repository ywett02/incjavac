package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.entity.FqName
import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import java.io.File
import javax.tools.StandardLocation

class StaleOutputCleaner(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage,
) {

    fun cleanStaleOutput(removedFiles: List<File>, incrementalJavaCompilerContext: IncrementalJavaCompilerContext) {
        val classesToRemove = getClassesToRemove(removedFiles, incrementalJavaCompilerContext)

        deleteClasses(classesToRemove.values)
        deleteDependencies(classesToRemove.keys)
        deleteFileToFqnEdge(removedFiles)
    }

    private fun getClassesToRemove(
        removedFiles: List<File>,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): Map<FqName, File> {
        val fileToFqnMap = fileToFqnMapInMemoryStorage.get()

        val fqnToRemove: Set<FqName> = removedFiles
            .flatMap { file ->
                fileToFqnMap.getOrDefault(file, emptySet())
            }.toSet()

        return incrementalJavaCompilerContext.classObjects
            .associate { javaFileObject ->
                FqName(
                    incrementalJavaCompilerContext.javaFileManager.inferBinaryName(
                        StandardLocation.CLASS_OUTPUT,
                        javaFileObject
                    )
                ) to File(javaFileObject.toUri())
            }.filter { (fqn, _) ->
                fqnToRemove.contains(fqn)
            }
    }

    private fun deleteClasses(classes: Collection<File>) {
        classes.forEach { file ->
            if (!file.delete()) {
                println("Failed to delete $file")
            } else {
                println("$file Failed to delete ")
            }
        }
    }

    private fun deleteDependencies(keys: Set<FqName>) {
        keys.forEach { fqn -> dependencyMapInMemoryStorage.remove(fqn) }
    }

    private fun deleteFileToFqnEdge(removedFiles: List<File>) {
        removedFiles.forEach { file -> fileToFqnMapInMemoryStorage.remove(file) }
    }
}