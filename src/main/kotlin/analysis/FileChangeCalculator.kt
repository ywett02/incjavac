package com.example.assignment.analysis

import com.example.assignment.entity.FileChanges
import com.example.assignment.storage.FileDigestInMemoryStorage
import com.example.assignment.util.md5
import java.io.File

class FileChangesCalculator(
    private val fileDigestInMemoryStorage: FileDigestInMemoryStorage
) {

    fun calculateFileChanges(sourceFiles: Set<File>): FileChanges {
        val currentMetadata = sourceFiles.associate { file -> file.absoluteFile to file.md5 }
        val previousMetadata = fileDigestInMemoryStorage.get()

        val fileChanges = FileChanges(
            calculateAddedAndModifiedFiles(currentMetadata, previousMetadata),
            calculateRemovedFiles(currentMetadata, previousMetadata)
        )

        fileDigestInMemoryStorage.set(currentMetadata)

        return fileChanges
    }

    private fun calculateAddedAndModifiedFiles(
        currentMetadata: Map<File, String>,
        previousMetadata: Map<File, String>
    ): List<File> = buildList {
        for ((file, digest) in currentMetadata) {
            if (previousMetadata[file] != digest) {
                add(file)
            }
        }
    }

    private fun calculateRemovedFiles(
        currentMetadata: Map<File, String>,
        previousMetadata: Map<File, String>
    ): List<File> = buildList {
        for ((file, _) in previousMetadata) {
            if (!currentMetadata.containsKey(file)) {
                add(file)
            }
        }
    }
}