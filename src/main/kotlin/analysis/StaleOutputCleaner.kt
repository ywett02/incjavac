package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.entity.FqName
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import java.io.File
import javax.tools.StandardLocation

class StaleOutputCleaner(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
) {

    private val deletedClasses: MutableSet<File> = mutableSetOf()

    fun cleanStaleOutput(removedFiles: List<File>, incrementalJavaCompilerContext: IncrementalJavaCompilerContext) {
        if (removedFiles.isEmpty()) {
            return
        }

        deleteClasses(getClassesToRemove(removedFiles, incrementalJavaCompilerContext).values)
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
                println("$file was deleted ")
                deletedClasses.add(file)
            }
        }
    }

    private fun deleteFileToFqnEdge(removedFiles: List<File>) {
        removedFiles.forEach { file -> fileToFqnMapInMemoryStorage.remove(file) }
    }
}