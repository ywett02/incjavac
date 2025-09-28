package com.example.javac.incremental

import com.example.javac.incremental.analysis.*
import com.example.javac.incremental.analysis.constant.ConstantDependencyMapCollectorFactory
import com.example.javac.incremental.entity.CompilationResult
import com.example.javac.incremental.entity.ExitCode
import com.example.javac.incremental.entity.ExitCode.COMPILATION_ERROR
import com.example.javac.incremental.entity.ExitCode.OK
import com.example.javac.incremental.entity.FileChanges
import com.example.javac.incremental.reporter.EventReporter
import com.sun.source.util.JavacTask
import java.io.File

class IncrementalJavaCompilerRunner(
    private val fileChangesTracker: FileChangesTracker,
    private val classpathChangesTracker: ClasspathChangesTracker,
    private val dirtyFilesCalculator: DirtyFilesCalculator,
    private val dependencyMapCollectorFactory: DependencyMapCollectorFactory,
    private val fileToFqnMapCollectorFactory: FileToFqnMapCollectorFactory,
    private val constantDependencyMapCollectorFactory: ConstantDependencyMapCollectorFactory,
    private val eventReporter: EventReporter
) {

    fun compile(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): ExitCode {
        try {
            val fileChanges = fileChangesTracker.trackFileChanges(incrementalJavaCompilerContext.sourceFiles)
            eventReporter.reportEvent(
                """Added or modified files: [${fileChanges.addedAndModifiedFiles.joinToString()}]
                |Removed files: [${fileChanges.removedFiles.joinToString()}]
            """.trimMargin()
            )
            val hasClasspathChanged =
                classpathChangesTracker.hasClasspathChanged(incrementalJavaCompilerContext.classpath)

            val exitCode =
                when (val compilationResult =
                    tryCompileIncrementally(fileChanges, hasClasspathChanged, incrementalJavaCompilerContext)) {
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

            incrementalJavaCompilerContext.compilationTransaction.cleanup(exitCode)
            return exitCode
        } catch (e: Throwable) {
            eventReporter.reportEvent("Compilation failed due to internal error: ${e.localizedMessage}")
            incrementalJavaCompilerContext.compilationTransaction.cleanup(ExitCode.INTERNAL_ERROR)
            return ExitCode.INTERNAL_ERROR
        }
    }

    private fun tryCompileIncrementally(
        fileChanges: FileChanges,
        hasClasspathChanged: Boolean,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): CompilationResult {
        try {
            if (!incrementalJavaCompilerContext.outputDir.exists()) {
                return CompilationResult.RequiresRecompilation("Previous output doest not exist")
            }

            if (hasClasspathChanged) {
                return CompilationResult.RequiresRecompilation("Classpath has changed")
            }

            val dirtyFiles = dirtyFilesCalculator.calculateDirtyFiles(fileChanges, incrementalJavaCompilerContext)
            eventReporter.run {
                reportEvent("Dirty source files: [${dirtyFiles.dirtySourceFiles.joinToString()}]")
                reportEvent("Dirty class files: [${dirtyFiles.dirtyClassFiles.joinToString()}]")
            }

            for (classFile in dirtyFiles.dirtyClassFiles) {
                incrementalJavaCompilerContext.compilationTransaction.deleteFile(classFile)
            }

            if (dirtyFiles.dirtySourceFiles.isEmpty()) {
                return CompilationResult.Success(OK)
            }

            return CompilationResult.Success(
                runCompilation(
                    dirtyFiles.dirtySourceFiles,
                    incrementalJavaCompilerContext
                )
            )
        } catch (e: Throwable) {
            eventReporter.reportEvent(
                "Compilation failed due to internal error: ${e.localizedMessage}"
            )

            return CompilationResult.Error(e)
        }
    }

    private fun runCompilation(
        filesToCompile: Set<File>,
        incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    ): ExitCode {
        val compilationUnits =
            incrementalJavaCompilerContext.javaFileManager.getJavaFileObjectsFromFiles(filesToCompile)
        val compilationOptions = createCompilationOptions(incrementalJavaCompilerContext)

        val javacTask = incrementalJavaCompilerContext.javaCompiler.getTask(
            null,
            incrementalJavaCompilerContext.javaFileManager,
            null,
            compilationOptions,
            null,
            compilationUnits
        ) as JavacTask

        javacTask.addTaskListener(fileToFqnMapCollectorFactory.create(javacTask.elements))
        javacTask.addTaskListener(
            dependencyMapCollectorFactory.create(
                javacTask.elements,
                incrementalJavaCompilerContext
            )
        )
        javacTask.addTaskListener(constantDependencyMapCollectorFactory.create(javacTask))

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
        val classpath = buildString {
            append(incrementalJavaCompilerContext.outputDir.absolutePath)

            if (incrementalJavaCompilerContext.classpath != null) {
                append(File.pathSeparator)
                append(incrementalJavaCompilerContext.classpath)
            }
        }

        return buildList {
            add("-cp")
            add(classpath)

            add("-d")
            add(incrementalJavaCompilerContext.outputDir.absolutePath)
        }
    }
}