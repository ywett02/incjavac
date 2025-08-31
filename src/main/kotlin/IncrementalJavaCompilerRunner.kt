package com.example.assignment

import com.example.assignment.analysis.DirtyFilesCalculator
import com.example.assignment.analysis.FileChangesCalculator
import com.example.assignment.analysis.FileToFqnMapCollector
import com.example.assignment.analysis.StaleOutputCleaner
import com.example.assignment.collector.DependencyMapCollector
import com.example.assignment.entity.CompilationResult
import com.example.assignment.entity.ExitCode
import com.example.assignment.entity.ExitCode.COMPILATION_ERROR
import com.example.assignment.entity.ExitCode.OK
import com.example.assignment.entity.FileChanges
import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import com.example.assignment.util.joinToString
import com.example.assignment.util.joinToString2
import com.sun.source.util.JavacTask
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
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
    private val staleOutputCleaner: StaleOutputCleaner,
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage,
) {

    fun compile(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): ExitCode =
        try {
            when (val compilationResult = tryCompileIncrementally(incrementalJavaCompilerContext)) {
                is CompilationResult.RequiresRecompilation -> {
                    logger.log(
                        Level.INFO,
                        "Non-incremental compilation will be performed: ${compilationResult.message}"
                    )

                    runCompilation(incrementalJavaCompilerContext.sourceFiles, incrementalJavaCompilerContext)
                }

                is CompilationResult.Error -> {
                    logger.log(
                        Level.INFO,
                        "Incremental compilation failed, non-incremental compilation will be performed"
                    )

                    runCompilation(incrementalJavaCompilerContext.sourceFiles, incrementalJavaCompilerContext)
                }

                is CompilationResult.Success -> {
                    logger.log(
                        Level.INFO,
                        "Incremental compilation completed"
                    )

                    compilationResult.exitCode
                }
            }
        } catch (e: Throwable) {
            logger.log(
                Level.WARNING,
                "Compilation failed due to internal error: ${e.message}"
            )
            ExitCode.INTERNAL_ERROR
        }

    private fun tryCompileIncrementally(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): CompilationResult {
        if (!fileToFqnMapInMemoryStorage.exists() || !dependencyMapInMemoryStorage.exists()) {
            return CompilationResult.RequiresRecompilation("Required metadata doest not exist")
        }

        return try {
            val fileChanges = fileChangesCalculator.calculateFileChanges(incrementalJavaCompilerContext.sourceFiles)
            logger.log(
                Level.INFO,
                """Added or modified files: [${fileChanges.addedAndModifiedFiles.joinToString()}]
                |Removed files: [${fileChanges.removedFiles.joinToString()}]
            """.trimMargin()
            )

            val dirtyFiles = dirtyFilesCalculator.calculateDirtyFiles(fileChanges)
            logger.log(
                Level.INFO,
                "Dirty files: [${dirtyFiles.joinToString()}]"
            )

            if (dirtyFiles.isEmpty()) {
                return CompilationResult.Success(OK)
            }

            val result = CompilationResult.Success(runCompilation(dirtyFiles, incrementalJavaCompilerContext))

            collectDependencies()
            cleanStaleOutput(fileChanges)

            return result
        } catch (e: Throwable) {
            logger.log(
                Level.WARNING,
                "Compilation failed due to internal error: ${e.message}"
            )
            CompilationResult.Error(e)
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

        val fileToFqnMapCollector = FileToFqnMapCollector(javacTask.elements)
        javacTask.addTaskListener(fileToFqnMapCollector)

        logger.log(
            Level.INFO,
            "javac running with arguments: [${compilationOptions.joinToString(separator = " ")} ${
                filesToCompile.joinToString(
                    separator = " ",
                    transform = { it.absolutePath })
            }}]"
        )

        val success = javacTask.call()

        val fileToFqnMap = fileToFqnMapCollector.fileToFqnMap
        logger.log(
            Level.INFO,
            """File to FQN map created: [
                |${fileToFqnMap.joinToString2()}
                |]""".trimMargin()
        )
        fileToFqnMapInMemoryStorage.set(fileToFqnMap)

        return if (success) {
            OK
        } else {
            COMPILATION_ERROR
        }
    }


    private fun createCompilationOptions(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): Iterable<String> {
        val classpath = buildString {
            append(incrementalJavaCompilerContext.src.absolutePath)

            if (incrementalJavaCompilerContext.classpath != null) {
                append(File.pathSeparator)
                append(incrementalJavaCompilerContext.classpath)
            }
        }

        return buildList<String> {
            add("-cp")
            add(classpath)

            if (incrementalJavaCompilerContext.directory != null) {
                add("-d")
                add(incrementalJavaCompilerContext.directory.absolutePath)
            }
        }
    }

    private fun collectDependencies() {
        fileManager.list(StandardLocation.CLASS_OUTPUT, "", setOf(JavaFileObject.Kind.CLASS), true).forEach {
            val depGraph = dependencyMapCollector.collectDependencies(File(it.toUri()))
            logger.log(
                Level.INFO,
                """Dependency graph created: [
                |${depGraph.joinToString()}
                |]""".trimMargin()
            )
            dependencyMapInMemoryStorage.set(depGraph)
        }
    }

    private fun cleanStaleOutput(changes: FileChanges) {
        staleOutputCleaner.cleanStaleOutput(changes.removedFiles, fileManager)
    }

    companion object {
        private val logger = Logger.getLogger("IncrementalJavaCompilerRunner")
    }
}