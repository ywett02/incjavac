package com.example.assignment.resource

import com.example.assignment.resource.impl.FileResource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackupResourceTest {

    @Test
    fun `when compilation succeeds, then backup directory is deleted`(@TempDir tempDir: File) {
        val outputDir = File(tempDir, "output")
        val backupDir = File(tempDir, "backup")
        outputDir.mkdirs()
        backupDir.mkdirs()
        val testFile = File(backupDir, "Test.class")
        testFile.writeText("test content")
        val fileResource = FileResource(backupDir, outputDir)
        
        fileResource.onSuccess()
        
        assertFalse(backupDir.exists())
    }

    @Test
    fun `when compilation fails, then files are restored and backup is deleted`(@TempDir tempDir: File) {
        val outputDir = File(tempDir, "output")
        val backupDir = File(tempDir, "backup")
        outputDir.mkdirs()
        backupDir.mkdirs()
        val testFile = File(backupDir, "Test.class")
        testFile.writeText("test content")
        val fileResource = FileResource(backupDir, outputDir)
        
        fileResource.onFailure()

        assertFalse(backupDir.exists())
        val restoredFile = File(outputDir, "Test.class")
        assertTrue(restoredFile.exists())
        assertEquals("test content", restoredFile.readText())
    }

    @Test
    fun `when backup has nested directory structure, then files are restored correctly`(@TempDir tempDir: File) {
        val outputDir = File(tempDir, "output")
        val backupDir = File(tempDir, "backup")
        outputDir.mkdirs()
        backupDir.mkdirs()
        val nestedDir = File(backupDir, "com/example")
        nestedDir.mkdirs()
        val testFile = File(nestedDir, "Test.class")
        testFile.writeText("test content")
        val fileResource = FileResource(backupDir, outputDir)
        
        fileResource.onFailure()

        assertFalse(backupDir.exists())
        val restoredFile = File(outputDir, "com/example/Test.class")
        assertTrue(restoredFile.exists())
        assertEquals("test content", restoredFile.readText())
    }

    @Test
    fun `when backup directory does not exist, then no exception is thrown`(@TempDir tempDir: File) {
        val outputDir = File(tempDir, "output")
        val backupDir = File(tempDir, "backup")
        outputDir.mkdirs()
        
        val fileResource = FileResource(backupDir, outputDir)

        fileResource.onSuccess()
        fileResource.onFailure()
    }
}
