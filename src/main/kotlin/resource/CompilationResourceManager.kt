package com.example.assignment.resource

import com.example.assignment.entity.ExitCode

interface CompilationResourceManager {

    fun registerResource(resource: CompilationResource)

    fun cleanup(exitCode: ExitCode)
}

