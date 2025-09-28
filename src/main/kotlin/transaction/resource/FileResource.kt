package com.example.assignment.transaction.resource

import com.example.assignment.transaction.CompilationResource
import com.google.common.jimfs.Jimfs
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileResource(
    private val file: File
) : CompilationResource {

    private val inMemoryFileSystem = Jimfs.newFileSystem()
    private val backupPath = inMemoryFileSystem.getPath("backup")

    init {
        Files.copy(
            file.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING
        )
    }

    override fun onSuccess() {
        inMemoryFileSystem.close()
    }

    override fun onFailure() {
        restoreFromBackup()
        inMemoryFileSystem.close()
    }

    private fun restoreFromBackup() {
        Files.move(
            backupPath, file.toPath(), StandardCopyOption.REPLACE_EXISTING
        )
    }
}