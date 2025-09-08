package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.entity.FqName
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import java.io.File
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class StaleOutputCleaner(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage
) {

    fun cleanStaleOutput(removedFiles: Set<File>, incrementalJavaCompilerContext: IncrementalJavaCompilerContext) {
        val staleData = staleData(removedFiles)

        deleteClassFiles(staleData.values.flatten(), incrementalJavaCompilerContext)
        deleteFileToFqnEdge(removedFiles)
    }

    private fun staleData(
        removedFiles: Set<File>,
    ): Map<File, Set<FqName>> =
        fileToFqnMapInMemoryStorage.getAll()
            .filter { (file, _) ->
                removedFiles.contains(file)
            }

    private fun deleteClassFiles(
        fqnList: List<FqName>,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ) {
        fqnList
            .map { fqn ->
                incrementalJavaCompilerContext.javaFileManager.getJavaFileForOutput(
                    StandardLocation.CLASS_OUTPUT,
                    fqn.id,
                    JavaFileObject.Kind.CLASS,
                    null
                )
            }.forEach { javaFileObject ->
                if (!javaFileObject.delete()) {
                    println("Failed to delete ${javaFileObject.name}")
                } else {
                    println("${javaFileObject.name} was deleted ")
                }
            }
    }

    private fun deleteFileToFqnEdge(removedFiles: Set<File>) {
        removedFiles.forEach { file -> fileToFqnMapInMemoryStorage.remove(file) }
    }
}