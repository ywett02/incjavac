package com.example.assignment.transaction

import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.EventReporter
import com.example.assignment.transaction.resource.FileResource
import java.io.File

class CompilationTransaction(private val eventReporter: EventReporter) {

    private val resources = mutableListOf<CompilationResource>()

    fun deleteFile(file: File) {
        registerResource(FileResource(file))
        file.delete()
    }

    fun registerResource(resource: CompilationResource) {
        resources.add(resource)
    }

    fun cleanup(exitCode: ExitCode) {
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
    }
}
