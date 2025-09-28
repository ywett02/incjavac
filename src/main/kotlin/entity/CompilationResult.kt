package com.example.javac.incremental.entity

sealed interface CompilationResult {

    data class Success(val exitCode: ExitCode) : CompilationResult

    data class Error(val cause: Throwable) : CompilationResult

    data class RequiresRecompilation(
        val message: String
    ) : CompilationResult
}