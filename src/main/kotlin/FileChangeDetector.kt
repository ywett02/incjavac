package com.example.assignment

import java.io.File

class FileChangesDetector(private val fileMetadataStore: FileMetadataStore) {

    fun calculateFileChanges(sourceFiles: List<File>): FileChanges {
        val currentMetadata = sourceFiles.associate { file -> file.absoluteFile to file.md5 }
        val previousMetadata = fileMetadataStore.read()

        val fileChanges = FileChanges(
            calculateAddedAndModifiedFiles(currentMetadata, previousMetadata),
            calculateRemovedFiles(currentMetadata, previousMetadata)
        )

        //TODO: metadata should be stored only if compilation succeed
        fileMetadataStore.store(currentMetadata)

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