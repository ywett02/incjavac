package com.example.assignment.resource.impl

import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.EventReporter
import com.example.assignment.resource.CompilationResource
import com.example.assignment.resource.CompilationResourceManager

class AutoCloseableResourceManager(private val eventReporter: EventReporter) : CompilationResourceManager {

    private val resources = mutableListOf<CompilationResource>()
    private var cleanupPerformed = false

    override fun registerResource(resource: CompilationResource) {
        if (cleanupPerformed) {
            throw IllegalStateException("Cannot register resources after cleanup has been performed")
        }

        resources.add(resource)
    }

    override fun cleanup(exitCode: ExitCode) {
        if (cleanupPerformed) {
            throw IllegalStateException("Cleanup has already been performed")
        }

        try {
            when (exitCode) {
                ExitCode.OK -> {
                    resources.forEach { resource ->
                        try {
                            resource.onSuccess()
                        } catch (e: Exception) {
                            eventReporter.reportEvent("Error during success cleanup: ${e.message}")
                        }
                    }
                }

                ExitCode.COMPILATION_ERROR, ExitCode.INTERNAL_ERROR -> {
                    resources.forEach { resource ->
                        try {
                            resource.onFailure()
                        } catch (e: Exception) {
                            eventReporter.reportEvent("Error during failure cleanup: ${e.message}")
                        }
                    }
                }
            }
        } finally {
            cleanupPerformed = true
        }
    }
}