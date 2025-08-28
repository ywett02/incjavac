package com.example.assignment

import java.util.logging.Level
import java.util.logging.Logger
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class IncrementalJavaCompilerRunner(private val fileChangesDetector: FileChangesDetector) {

    fun compile(incrementalJavaCompilerArguments: IncrementalJavaCompilerArguments): Int {
        val fileChanges = fileChangesDetector.calculateFileChanges(incrementalJavaCompilerArguments.sourceFiles)
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

        return compiler.run(null, null, null, *javaCompilerArguments.toTypedArray())
    }

    companion object {
        private val logger = Logger.getLogger("IncrementalJavaCompilerRunner")
    }
}