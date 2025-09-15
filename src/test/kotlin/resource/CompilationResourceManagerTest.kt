package com.example.assignment.resource

import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.NoOpReporter
import com.example.assignment.resource.impl.AutoCloseableResourceManager
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompilationResourceManagerTest {

    @Test
    fun `when compilation succeeds, then onSuccess is called`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val testResource = TestResource()
        
        resourceManager.registerResource(testResource)
        resourceManager.cleanup(ExitCode.OK)
        
        assertTrue(testResource.successCalled)
        assertFalse(testResource.failureCalled)
    }

    @Test
    fun `when compilation fails, then onFailure is called`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val testResource = TestResource()
        
        resourceManager.registerResource(testResource)
        resourceManager.cleanup(ExitCode.COMPILATION_ERROR)
        
        assertFalse(testResource.successCalled)
        assertTrue(testResource.failureCalled)
    }

    @Test
    fun `when internal error occurs, then onFailure is called`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val testResource = TestResource()
        
        resourceManager.registerResource(testResource)
        resourceManager.cleanup(ExitCode.INTERNAL_ERROR)
        
        assertFalse(testResource.successCalled)
        assertTrue(testResource.failureCalled)
    }

    @Test
    fun `when multiple resources are registered, then all are cleaned up`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val resource1 = TestResource()
        val resource2 = TestResource()
        
        resourceManager.registerResource(resource1)
        resourceManager.registerResource(resource2)
        resourceManager.cleanup(ExitCode.OK)
        
        assertTrue(resource1.successCalled)
        assertTrue(resource2.successCalled)
    }

    @Test
    fun `when registering resources after cleanup, then exception is thrown`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val testResource = TestResource()
        
        resourceManager.registerResource(testResource)
        resourceManager.cleanup(ExitCode.OK)
        
        assertThrows<IllegalStateException> {
            resourceManager.registerResource(TestResource())
        }
    }

    @Test
    fun `when cleanup is called twice, then exception is thrown`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val testResource = TestResource()
        
        resourceManager.registerResource(testResource)
        resourceManager.cleanup(ExitCode.OK)
        
        assertThrows<IllegalStateException> {
            resourceManager.cleanup(ExitCode.OK)
        }
    }

    @Test
    fun `when one resource fails during cleanup, then other resources are still cleaned up`() {
        val resourceManager = AutoCloseableResourceManager(NoOpReporter)
        val failingResource = FailingTestResource()
        val normalResource = TestResource()
        
        resourceManager.registerResource(failingResource)
        resourceManager.registerResource(normalResource)
        
        // Should not throw exception
        resourceManager.cleanup(ExitCode.OK)
        
        assertTrue(normalResource.successCalled)
    }

    private class TestResource : CompilationResource {
        var successCalled = false
        var failureCalled = false

        override fun onSuccess() {
            successCalled = true
        }

        override fun onFailure() {
            failureCalled = true
        }
    }

    private class FailingTestResource : CompilationResource {
        override fun onSuccess() {
            throw RuntimeException("Test failure")
        }

        override fun onFailure() {
            throw RuntimeException("Test failure")
        }
    }
}
