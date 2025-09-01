package com.example.assignment.analysis

import com.example.assignment.IncrementalJavaCompilerContext
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import java.io.File
import javax.tools.JavaFileObject
import javax.tools.StandardLocation

class StaleOutputCleaner(
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
) {

    fun cleanStaleOutput(removedFiles: Set<File>, incrementalJavaCompilerContext: IncrementalJavaCompilerContext) {
        deleteClassFiles(removedFiles, incrementalJavaCompilerContext)
        deleteFileToFqnEdge(removedFiles)
    }

    private fun deleteClassFiles(
        removedFiles: Set<File>,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ) {
        fileToFqnMapInMemoryStorage.get()
            .filter { (file, _) ->
                removedFiles.contains(file)
            }
            .values
            .flatten()
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