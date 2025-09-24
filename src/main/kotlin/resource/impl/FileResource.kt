package com.example.assignment.resource.impl

import com.example.assignment.resource.CompilationResource
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class FileResource(
    private val backupDir: File,
    private val outputDir: File
) : CompilationResource {

    override fun onSuccess() {
        if (backupDir.exists()) {
            backupDir.deleteRecursively()
        }
    }

    override fun onFailure() {
        if (backupDir.exists()) {
            restoreFromBackup()
            backupDir.deleteRecursively()
        }
    }

    private fun restoreFromBackup() {
        backupDir.walk()
            .filter { it.isFile }
            .forEach { backupFile ->
                val relativePath = backupFile.relativeTo(backupDir)
                val targetFile = outputDir.resolve(relativePath.path)

                targetFile.parentFile?.mkdirs()

                Files.move(
                    backupFile.toPath(),
                    targetFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING
                )
            }
    }
}