package com.example.assignment

import com.example.assignment.analysis.FileChangesCalculator
import com.example.assignment.storage.FileDigestStorage
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.FileOptionHandler
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger

class IncrementalJavaCompilerCommand private constructor() {

    @Option(
        name = "-src",
        usage = "Source directory to search for Java files",
        required = true,
        handler = FileOptionHandler::class
    )
    private var _src: File? = null
    val src: File
        get() = requireNotNull(_src)

    @Option(
        name = "-cp",
        aliases = ["-classpath"],
        usage = "List of directories and JAR/ZIP archives to search for class files",
        required = false,
    )
    var classpath: String? = null

    @Option(
        name = "-d",
        usage = "Destination for generated class files",
        required = false,
        handler = FileOptionHandler::class
    )
    var directory: File? = null

    @Option(
        name = "-cd",
        aliases = ["-cacheDir"],
        usage = "Directory where cache files will be stored",
        required = true,
        handler = FileOptionHandler::class
    )
    var cacheDir: File? = null

    companion object {
        private val logger: Logger =
            Logger.getLogger("IncrementalJavaCompilerCommand")

        fun run(args: Array<String>): Int {
            logger.log(Level.INFO, "incJavac running with arguments: [${args.joinToString(separator = " ")}]")
            val incJavaCompilerArguments = parseArguments(args)

            val incrementalJavaCompilerRunner =
                IncrementalJavaCompilerRunner(FileChangesCalculator(FileDigestStorage.create(incJavaCompilerArguments.cacheDir)))

            return incrementalJavaCompilerRunner.compile(incJavaCompilerArguments)
        }

        private fun parseArguments(args: Array<String>): IncrementalJavaCompilerArguments {
            val incrementalJavaCompilerCommand = IncrementalJavaCompilerCommand()
            val parser = CmdLineParser(incrementalJavaCompilerCommand)

            return try {
                parser.parseArgument(*args)
                IncrementalJavaCompilerArguments(
                    incrementalJavaCompilerCommand.src,
                    requireNotNull(incrementalJavaCompilerCommand.cacheDir),
                    requireNotNull(incrementalJavaCompilerCommand.directory),
                    incrementalJavaCompilerCommand.classpath
                )
            } catch (cmdException: CmdLineException) {
                System.err.println(cmdException.message)
                parser.printUsage(System.err)

                throw cmdException
            }
        }
    }
}