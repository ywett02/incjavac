package com.example.assignment.analysis

import com.example.assignment.FileChanges
import com.example.assignment.storage.FileDigestStorage
import com.example.assignment.util.md5
import java.io.File

class FileChangesCalculator(
    private val fileDigestStorage: FileDigestStorage
) {

    fun calculateFileChanges(sourceFiles: List<File>): FileChanges {
        val currentMetadata = sourceFiles.associate { file -> file.absoluteFile to file.md5 }
        //TODO: handle the first run
        val previousMetadata = fileDigestStorage.load() ?: mutableMapOf()

        val fileChanges = FileChanges(
            calculateAddedAndModifiedFiles(currentMetadata, previousMetadata),
            calculateRemovedFiles(currentMetadata, previousMetadata)
        )

        //TODO: metadata could be stored only if compilation succeed
        fileDigestStorage.save(currentMetadata)

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