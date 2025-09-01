package com.example.assignment

import com.example.assignment.analysis.DependencyMapCollector
import com.example.assignment.analysis.DirtyFilesCalculator
import com.example.assignment.analysis.FileChangesCalculator
import com.example.assignment.analysis.StaleOutputCleaner
import com.example.assignment.entity.ExitCode
import com.example.assignment.storage.DependencyMapInMemoryStorage
import com.example.assignment.storage.FileDigestInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import org.kohsuke.args4j.CmdLineException
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.FileOptionHandler
import java.io.File
import javax.tools.ToolProvider

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
    private var _classpath: String? = null
    val classpath: String?
        get() = _classpath

    @Option(
        name = "-d",
        usage = "Destination for generated class files",
        required = false,
        handler = FileOptionHandler::class
    )
    private var _directory: File? = null
    val directory: File?
        get() = _directory

    @Option(
        name = "-cd",
        aliases = ["-cacheDir"],
        usage = "Directory where cache files will be stored",
        required = false,
        handler = FileOptionHandler::class
    )
    private var _cacheDir: File? = null
    val cacheDir: File
        get() = _cacheDir ?: src.parentFile.resolve(DEFAULT_CACHE_DIR_NAME)

    @Option(
        name = "-debug",
        usage = "Enable debug mode",
        required = false
    )
    private var _debug: Boolean = false
    val debug: Boolean
        get() = _debug

    companion object {
        private const val DEFAULT_CACHE_DIR_NAME = "cache"

        fun run(args: Array<String>): ExitCode {
            val incrementalJavaCompilerCommand = createCommand(args)
            val eventReporter = EventReporter(incrementalJavaCompilerCommand.debug)
            eventReporter.reportEvent("incJavac running with arguments: [${args.joinToString(separator = " ")}]")

            val fileDigestInMemoryStorage = FileDigestInMemoryStorage.create(incrementalJavaCompilerCommand.cacheDir)
            val fileToFqnMapInMemoryStorage =
                FileToFqnMapInMemoryStorage.create(incrementalJavaCompilerCommand.cacheDir)
            val dependencyMapInMemoryStorage =
                DependencyMapInMemoryStorage.create(incrementalJavaCompilerCommand.cacheDir)

            val javaCompiler = ToolProvider.getSystemJavaCompiler()
            val incrementalJavaCompilerRunner =
                IncrementalJavaCompilerRunner(
                    javaCompiler,
                    javaCompiler.getStandardFileManager(null, null, null),
                    FileChangesCalculator(fileDigestInMemoryStorage),
                    DirtyFilesCalculator(fileToFqnMapInMemoryStorage, dependencyMapInMemoryStorage),
                    DependencyMapCollector(),
                    StaleOutputCleaner(fileToFqnMapInMemoryStorage, dependencyMapInMemoryStorage),
                    fileToFqnMapInMemoryStorage,
                    dependencyMapInMemoryStorage,
                    eventReporter
                )

            val incrementalJavaCompilerContext = IncrementalJavaCompilerContext(
                incrementalJavaCompilerCommand.src,
                incrementalJavaCompilerCommand.directory,
                incrementalJavaCompilerCommand.classpath
            )

            val exitCode = incrementalJavaCompilerRunner.compile(incrementalJavaCompilerContext)

            if (exitCode == ExitCode.OK) {
                fileDigestInMemoryStorage.close()
                fileToFqnMapInMemoryStorage.close()
                dependencyMapInMemoryStorage.close()
            }

            return exitCode
        }

        private fun createCommand(args: Array<String>): IncrementalJavaCompilerCommand {
            val incrementalJavaCompilerCommand = IncrementalJavaCompilerCommand()
            val parser = CmdLineParser(incrementalJavaCompilerCommand)

            return try {
                parser.parseArgument(*args)
                return incrementalJavaCompilerCommand
            } catch (cmdException: CmdLineException) {
                System.err.println(cmdException.message)
                parser.printUsage(System.err)

                throw cmdException
            }
        }
    }
}