package com.example.assignment.analysis

import com.example.assignment.entity.FileChanges
import com.example.assignment.storage.FileDigestInMemoryStorage
import com.example.assignment.util.md5
import java.io.File

class FileChangesTracker(
    private val fileDigestInMemoryStorage: FileDigestInMemoryStorage
) {

    fun trackFileChanges(sourceFiles: Set<File>): FileChanges {
        val currentMetadata = sourceFiles.associate { file -> file.absoluteFile to file.md5 }
        val previousMetadata = fileDigestInMemoryStorage.getAll()

        val fileChanges = FileChanges(
            getAndUpdateAddedAndModifiedFiles(currentMetadata, previousMetadata),
            getAndUpdateRemovedFiles(currentMetadata, previousMetadata)
        )

        return fileChanges
    }

    private fun getAndUpdateAddedAndModifiedFiles(
        currentMetadata: Map<File, String>,
        previousMetadata: Map<File, String>
    ): Set<File> = buildSet {
        for ((file, digest) in currentMetadata) {
            if (previousMetadata[file] != digest) {
                fileDigestInMemoryStorage.put(file, digest)
                add(file)
            }
        }
    }

    private fun getAndUpdateRemovedFiles(
        currentMetadata: Map<File, String>,
        previousMetadata: Map<File, String>
    ): Set<File> = buildSet {
        for ((file, _) in previousMetadata) {
            if (!currentMetadata.containsKey(file)) {
                fileDigestInMemoryStorage.remove(file)
                add(file)
            }
        }
    }
}