package com.example.assignment

import com.example.assignment.analysis.*
import com.example.assignment.entity.CompilationResult
import com.example.assignment.entity.ExitCode
import com.example.assignment.entity.ExitCode.COMPILATION_ERROR
import com.example.assignment.entity.ExitCode.OK
import com.example.assignment.entity.FileChanges
import com.sun.source.util.JavacTask
import java.io.File
import javax.tools.JavaCompiler
import javax.tools.JavaFileObject
import javax.tools.StandardJavaFileManager
import javax.tools.StandardLocation

class IncrementalJavaCompilerRunner(
    private val javac: JavaCompiler,
    private val fileManager: StandardJavaFileManager,
    private val fileChangesCalculator: FileChangesCalculator,
    private val dirtyFilesCalculator: DirtyFilesCalculator,
    private val dependencyMapCollector: DependencyMapCollector,
    private val fileToFqnMapCollectorFactory: FileToFqnMapCollectorFactory,
    private val staleOutputCleaner: StaleOutputCleaner,
    private val eventReporter: EventReporter
) {

    fun compile(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): ExitCode {
        try {
            val exitCode = when (val compilationResult = tryCompileIncrementally(incrementalJavaCompilerContext)) {
                is CompilationResult.RequiresRecompilation -> {
                    eventReporter.reportEvent("Non-incremental compilation will be performed: ${compilationResult.message}")
                    runCompilation(incrementalJavaCompilerContext.sourceFiles, incrementalJavaCompilerContext)
                }

                is CompilationResult.Error -> {
                    eventReporter.reportEvent(
                        "Incremental compilation failed, non-incremental compilation will be performed"
                    )

                    runCompilation(incrementalJavaCompilerContext.sourceFiles, incrementalJavaCompilerContext)
                }

                is CompilationResult.Success -> {
                    eventReporter.reportEvent(
                        "Incremental compilation completed"
                    )

                    compilationResult.exitCode
                }
            }
            collectDependencies(incrementalJavaCompilerContext)

            return exitCode
        } catch (e: Throwable) {
            eventReporter.reportEvent("Compilation failed due to internal error: ${e.message}")
            return ExitCode.INTERNAL_ERROR
        }
    }

    private fun tryCompileIncrementally(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): CompilationResult {
        try {
            val fileChanges = fileChangesCalculator.calculateFileChanges(incrementalJavaCompilerContext.sourceFiles)
            eventReporter.reportEvent(
                """Added or modified files: [${fileChanges.addedAndModifiedFiles.joinToString()}]
                |Removed files: [${fileChanges.removedFiles.joinToString()}]
            """.trimMargin()
            )

            if (!incrementalJavaCompilerContext.cacheDir.exists()) {
                return CompilationResult.RequiresRecompilation("Required metadata doest not exist")
            }

            val dirtyFiles = dirtyFilesCalculator.calculateDirtyFiles(fileChanges)
            eventReporter.reportEvent(
                "Dirty files: [${dirtyFiles.joinToString()}]"
            )

            if (dirtyFiles.isEmpty()) {
                return CompilationResult.Success(OK)
            }

            val result = CompilationResult.Success(runCompilation(dirtyFiles, incrementalJavaCompilerContext))
            cleanStaleOutput(fileChanges)

            return result
        } catch (e: Throwable) {
            eventReporter.reportEvent(
                "Compilation failed due to internal error: ${e.message}"
            )

            return CompilationResult.Error(e)
        }
    }

    private fun runCompilation(
        filesToCompile: Set<File>,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): ExitCode {
        val compilationUnits = fileManager.getJavaFileObjectsFromFiles(filesToCompile)
        val compilationOptions = createCompilationOptions(incrementalJavaCompilerContext)

        val javacTask = javac.getTask(
            null,
            fileManager,
            null,
            compilationOptions,
            null,
            compilationUnits
        ) as JavacTask

        javacTask.addTaskListener(fileToFqnMapCollectorFactory.create(javacTask.elements))

        eventReporter.reportEvent(
            "javac running with arguments: [${compilationOptions.joinToString(separator = " ")} ${
                filesToCompile.joinToString(
                    separator = " ",
                    transform = { it.absolutePath })
            }}]"
        )

        return if (javacTask.call()) {
            OK
        } else {
            COMPILATION_ERROR
        }
    }


    private fun createCompilationOptions(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): Iterable<String> {
        val classFiles = fileManager.list(StandardLocation.CLASS_OUTPUT, "", setOf(JavaFileObject.Kind.CLASS), true)
            .map { javaFileObject ->
                File(javaFileObject.toUri())
            }.joinToString(separator = File.pathSeparator, transform = { file -> file.absolutePath })

        val classpath = buildString {
            append(classFiles)

            if (incrementalJavaCompilerContext.classpath != null) {
                append(File.pathSeparator)
                append(incrementalJavaCompilerContext.classpath)
            }
        }

        return buildList<String> {
            add("-cp")
            add(classpath)

            add("-d")
            add(incrementalJavaCompilerContext.directory.absolutePath)
        }
    }

    private fun collectDependencies(incrementalJavaCompilerContext: IncrementalJavaCompilerContext) {
        dependencyMapCollector.collectDependencies(incrementalJavaCompilerContext.directory)
    }

    private fun cleanStaleOutput(changes: FileChanges) {
        staleOutputCleaner.cleanStaleOutput(changes.removedFiles, fileManager)
    }
}