package com.example.assignment.transaction.impl

import com.example.assignment.transaction.CompilationResource
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileResource private constructor(
    private val file: File, private val backupFile: File
) : CompilationResource {

    override fun onSuccess() {
        if (backupFile.exists()) {
            backupFile.delete()
        }
    }

    override fun onFailure() {
        if (backupFile.exists()) {
            restoreFromBackup()
            backupFile.delete()
        }
    }

    private fun restoreFromBackup() {
        Files.move(
            backupFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING
        )
    }

    companion object {
        fun create(file: File): FileResource {
            val backupFile: File = Files.createTempDirectory("backup").toFile()
            Files.move(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING)

            return FileResource(file, backupFile)
        }
    }
}