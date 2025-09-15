package com.example.assignment.resource

import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.NoOpReporter
import com.example.assignment.resource.impl.AutoCloseableResourceManager
import com.example.assignment.resource.impl.asResource
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.Closeable
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ResourceManagementExampleTest {

    @Test
    fun `when resources are registered and compilation succeeds, then all resources are cleaned up correctly`(@TempDir tempDir: File) {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val mockStorage = MockStorage("storage1")
        val mockBackup = MockBackupResource(tempDir)
        val mockCustomResource = MockCustomResource()
        resourceManager.registerResource(mockStorage.asResource())
        resourceManager.registerResource(mockBackup)
        resourceManager.registerResource(mockCustomResource)

        resourceManager.cleanup(ExitCode.OK)

        assertTrue(mockStorage.closed)
        assertTrue(mockBackup.successCalled)
        assertFalse(mockBackup.failureCalled)
        assertTrue(mockCustomResource.successCalled)
        assertFalse(mockCustomResource.failureCalled)
    }

    @Test
    fun `when resources are registered and compilation fails, then failure cleanup is performed`(@TempDir tempDir: File) {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val mockStorage = MockStorage("storage2")
        val mockBackup = MockBackupResource(tempDir)
        val mockCustomResource = MockCustomResource()
        resourceManager.registerResource(mockStorage.asResource())
        resourceManager.registerResource(mockBackup)
        resourceManager.registerResource(mockCustomResource)

        resourceManager.cleanup(ExitCode.COMPILATION_ERROR)

        assertTrue(mockStorage.closed)
        assertFalse(mockBackup.successCalled)
        assertTrue(mockBackup.failureCalled)
        assertFalse(mockCustomResource.successCalled)
        assertTrue(mockCustomResource.failureCalled)
    }

    private class MockStorage(private val name: String) : Closeable {
        var closed = false
        
        override fun close() {
            closed = true
            println("MockStorage $name closed")
        }
    }

    private class MockBackupResource(private val tempDir: File) : CompilationResource {
        var successCalled = false
        var failureCalled = false
        
        override fun onSuccess() {
            successCalled = true
            println("MockBackupResource: Success cleanup")
        }
        
        override fun onFailure() {
            failureCalled = true
            println("MockBackupResource: Failure cleanup - would restore files")
        }
    }

    private class MockCustomResource : CompilationResource {
        var successCalled = false
        var failureCalled = false
        
        override fun onSuccess() {
            successCalled = true
            println("MockCustomResource: Success cleanup - updating cache")
        }
        
        override fun onFailure() {
            failureCalled = true
            println("MockCustomResource: Failure cleanup - rolling back changes")
        }
    }
}
