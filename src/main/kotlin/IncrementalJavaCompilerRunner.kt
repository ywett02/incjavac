package com.example.assignment

import com.example.assignment.analysis.FileChangesCalculator
import com.example.assignment.analysis.FileToFqnMapCollector
import com.example.assignment.analysis.StaleOutputCleaner
import com.example.assignment.collector.DependencyMapCollector
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
import javax.tools.StandardLocation
import javax.tools.ToolProvider

class IncrementalJavaCompilerRunner(
    private val fileChangesCalculator: FileChangesCalculator,
    private val fileToFqnMapInMemoryStorage: FileToFqnMapInMemoryStorage,
    private val dependencyMapInMemoryStorage: DependencyMapInMemoryStorage
) {

    fun compile(incrementalJavaCompilerContext: IncrementalJavaCompilerContext): Boolean {


        val fileChanges = fileChangesCalculator.calculateFileChanges(incrementalJavaCompilerContext.sourceFiles)
        logger.log(
            Level.INFO,
            """Added or modified files: [${fileChanges.addedAndModifiedFiles.joinToString()}]
                |Removed files: [${fileChanges.removedFiles.joinToString()}]
            """.trimMargin()
        )

        val javaCompilerArguments = incrementalJavaCompilerContext.toJavaCompilerArguments()
        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
        val fileManager = compiler.getStandardFileManager(null, null, null)
        val compilationUnits = fileManager.getJavaFileObjectsFromFiles(incrementalJavaCompilerContext.sourceFiles)

        val javacTask = compiler.getTask(
            null,
            fileManager,
            null,
            javaCompilerArguments,
            null,
            compilationUnits
        ) as JavacTask

        val fileToFqnMapCollector: FileToFqnMapCollector = FileToFqnMapCollector(javacTask.elements)
        javacTask.addTaskListener(fileToFqnMapCollector)

        logger.log(
            Level.INFO,
            "javac running with arguments: [${javaCompilerArguments.joinToString(separator = " ")}]"
        )
        val success = javacTask.call()

        if (success) {

            StaleOutputCleaner(
                fileToFqnMapInMemoryStorage,
                dependencyMapInMemoryStorage
            ).cleanStaleOutput(fileChanges.removedFiles, fileManager)

            fileManager.list(StandardLocation.CLASS_OUTPUT, "", setOf(JavaFileObject.Kind.CLASS), true).forEach {
                val depGraph = DependencyMapCollector().collectDependencies(File(it.toUri()))
                logger.log(
                    Level.INFO,
                    """Dependency graph created: [
                |${depGraph.joinToString()}
                |]""".trimMargin()
                )
                dependencyMapInMemoryStorage.set(depGraph)
            }

            val fileToFqnMap = fileToFqnMapCollector.fileToFqnMap
            logger.log(
                Level.INFO,
                """File to FQN map created: [
                |${fileToFqnMap.joinToString2()}
                |]""".trimMargin()
            )
            fileToFqnMapInMemoryStorage.set(fileToFqnMap)
        }

        return success
    }

    companion object {
        private val logger = Logger.getLogger("IncrementalJavaCompilerRunner")
    }
}