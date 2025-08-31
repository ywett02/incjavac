package com.example.assignment

import com.example.assignment.analysis.FileChangesCalculator
import com.example.assignment.collector.DependencyMapCollector
import com.example.assignment.storage.DependencyMapStorage
import com.example.assignment.util.joinToString
import java.util.logging.Level
import java.util.logging.Logger
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class IncrementalJavaCompilerRunner(
    private val fileChangesCalculator: FileChangesCalculator,
) {

    fun compile(incrementalJavaCompilerArguments: IncrementalJavaCompilerArguments): Int {
        val fileChanges = fileChangesCalculator.calculateFileChanges(incrementalJavaCompilerArguments.sourceFiles)
        logger.log(
            Level.INFO,
            """Added or modified files: [${fileChanges.addedAndModifiedFiles.joinToString()}]
                |Removed files: [${fileChanges.removedFiles.joinToString()}]
            """.trimMargin()
        )

        val javaCompilerArguments = incrementalJavaCompilerArguments.toJavaCompilerArguments()
        val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()
        logger.log(
            Level.INFO,
            "javac running with arguments: [${javaCompilerArguments.joinToString(separator = " ")}]"
        )
        val result = compiler.run(null, null, null, *javaCompilerArguments.toTypedArray())

        val depGraph = DependencyMapCollector().collectDependencies(incrementalJavaCompilerArguments.directory)
        logger.log(
            Level.INFO,
            """Dependency graph created: [
                |${depGraph.joinToString()}
                |]""".trimMargin()
        )
        val graphStore = DependencyMapStorage.create(incrementalJavaCompilerArguments.cacheDir)
        graphStore.save(depGraph)

        return result
    }

    companion object {
        private val logger = Logger.getLogger("IncrementalJavaCompilerRunner")
    }
}