package com.example.assignment

import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.FileOptionHandler
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import javax.tools.JavaCompiler
import javax.tools.ToolProvider

class IncrementalJavaCompilerCommand private constructor() {

    @Option(
        name = "-src",
        usage = "Source directory to search for Java files",
        required = true,
        handler = FileOptionHandler::class
    )
    private var src: File? = null

    @Option(
        name = "-cp",
        aliases = ["-classpath"],
        usage = "List of directories and JAR/ZIP archives to search for class files",
        required = false,
    )
    private var classpath: String? = null

    @Option(
        name = "-d",
        usage = "Destination for generated class files",
        required = false,
        handler = FileOptionHandler::class
    )
    private var directory: File? = null

    companion object {
        private val logger: java.util.logging.Logger =
            Logger.getLogger("IncrementalJavaCompilerCommand")

        fun run(args: Array<String>): Int {
            logger.log(Level.INFO, "incJavac running with arguments: [${args.joinToString(separator = " ")}]")
            val incJavaCompilerArguments = parseArguments(args)

            val javaCompilerArguments = incJavaCompilerArguments.toJavaCompilerArguments()
            val compiler: JavaCompiler = ToolProvider.getSystemJavaCompiler()

            logger.log(
                Level.INFO,
                "javac running with arguments: [${javaCompilerArguments.joinToString(separator = " ")}]"
            )
            return compiler.run(null, null, null, *javaCompilerArguments.toTypedArray())
        }

        private fun parseArguments(args: Array<String>): IncrementalJavaCompilerArguments {
            val incrementalJavaCompilerCommand = IncrementalJavaCompilerCommand()
            val parser = CmdLineParser(incrementalJavaCompilerCommand)

            return try {
                parser.parseArgument(*args)
                IncrementalJavaCompilerArguments(
                    requireNotNull(incrementalJavaCompilerCommand.src),
                    incrementalJavaCompilerCommand.classpath,
                    incrementalJavaCompilerCommand.directory
                )
            } catch (cmdException: CmdLineException) {
                System.err.println(cmdException.message)
                parser.printUsage(System.err)

                throw cmdException
            }
        }
    }
}