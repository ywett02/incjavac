package com.example.assignment.resource

import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.NoOpReporter
import com.example.assignment.resource.impl.AutoCloseableResourceManager
import com.example.assignment.resource.impl.asResource
import com.example.assignment.storage.Storage
import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue


class ResourceManagementExampleTest {

    @Test
    fun `when resources are registered and compilation succeeds, then all resources are cleaned up correctly`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val mockStorage = MockStorage()
        val mockBackup = MockBackupResource()
        val mockCustomResource = MockCustomResource()
        resourceManager.registerResource(mockStorage.asResource())
        resourceManager.registerResource(mockBackup)
        resourceManager.registerResource(mockCustomResource)

        resourceManager.cleanup(ExitCode.OK)

        assertTrue(mockStorage.flushed)
        assertTrue(mockBackup.successCalled)
        assertFalse(mockBackup.failureCalled)
        assertTrue(mockCustomResource.successCalled)
        assertFalse(mockCustomResource.failureCalled)
    }

    @Test
    fun `when resources are registered and compilation fails, then nothing is flushed`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val mockStorage = MockStorage()
        val mockBackup = MockBackupResource()
        val mockCustomResource = MockCustomResource()
        resourceManager.registerResource(mockStorage.asResource())
        resourceManager.registerResource(mockBackup)
        resourceManager.registerResource(mockCustomResource)

        resourceManager.cleanup(ExitCode.COMPILATION_ERROR)

        assertFalse(mockStorage.flushed)
        assertFalse(mockBackup.successCalled)
        assertTrue(mockBackup.failureCalled)
        assertFalse(mockCustomResource.successCalled)
        assertTrue(mockCustomResource.failureCalled)
    }

    private class MockStorage() : Storage {
        var flushed = false

        override fun flush() {
            flushed = true
        }
    }

    private class MockBackupResource() : CompilationResource {
        var successCalled = false
        var failureCalled = false
        
        override fun onSuccess() {
            successCalled = true
        }
        
        override fun onFailure() {
            failureCalled = true
        }
    }

    private class MockCustomResource : CompilationResource {
        var successCalled = false
        var failureCalled = false
        
        override fun onSuccess() {
            successCalled = true
        }
        
        override fun onFailure() {
            failureCalled = true
        }
    }
}
