package com.example.assignment

import com.example.assignment.analysis.*
import com.example.assignment.entity.ExitCode
import com.example.assignment.storage.ClasspathDigestInMemoryStorage
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
    val directory: File
        get() = _directory ?: src.parentFile.resolve(DEFAULT_BUILD_DIR_NAME).resolve(DEFAULT_DIRECTORY_DIR_NAME)

    @Option(
        name = "-cd",
        aliases = ["-cacheDir"],
        usage = "Directory where cache files will be stored",
        required = false,
        handler = FileOptionHandler::class
    )
    private var _cacheDir: File? = null
    val cacheDir: File
        get() = _cacheDir ?: src.parentFile.resolve(DEFAULT_BUILD_DIR_NAME).resolve(DEFAULT_CACHE_DIR_NAME)

    @Option(
        name = "--debug",
        usage = "Enable debug mode",
        required = false
    )
    private var _debug: Boolean = false
    val debug: Boolean
        get() = _debug

    companion object {
        private const val DEFAULT_BUILD_DIR_NAME = "build"
        private const val DEFAULT_CACHE_DIR_NAME = "cache"
        private const val DEFAULT_DIRECTORY_DIR_NAME = "classes"
        private const val DEFAULT_METADATA_DIR_NAME = "metadata"

        fun run(args: Array<String>): ExitCode {
            val incrementalJavaCompilerCommand = createCommand(args)
            val eventReporter = EventReporter(incrementalJavaCompilerCommand.debug)
            eventReporter.reportEvent("incJavac running with arguments: [${args.joinToString(separator = " ")}]")

            val incrementalJavaCompilerContext = IncrementalJavaCompilerContext(
                src = incrementalJavaCompilerCommand.src,
                outputDir = incrementalJavaCompilerCommand.cacheDir.resolve(DEFAULT_DIRECTORY_DIR_NAME),
                metadataDir = incrementalJavaCompilerCommand.cacheDir.resolve(DEFAULT_METADATA_DIR_NAME),
                classpath = incrementalJavaCompilerCommand.classpath,
                javaCompiler = ToolProvider.getSystemJavaCompiler()
            )

            val fileDigestInMemoryStorage = FileDigestInMemoryStorage.create(incrementalJavaCompilerContext.metadataDir)
            val classpathDigestInMemoryStorage =
                ClasspathDigestInMemoryStorage.create(incrementalJavaCompilerContext.metadataDir)
            val fileToFqnMapInMemoryStorage =
                FileToFqnMapInMemoryStorage.create(incrementalJavaCompilerContext.metadataDir)
            val dependencyMapInMemoryStorage =
                DependencyMapInMemoryStorage.create(incrementalJavaCompilerContext.metadataDir)

            val incrementalJavaCompilerRunner =
                IncrementalJavaCompilerRunner(
                    FileChangesCalculator(fileDigestInMemoryStorage),
                    ClasspathChangeCalculator(classpathDigestInMemoryStorage),
                    DirtyFilesCalculator(fileToFqnMapInMemoryStorage, dependencyMapInMemoryStorage),
                    DependencyMapCollector(dependencyMapInMemoryStorage),
                    FileToFqnMapCollectorFactory(fileToFqnMapInMemoryStorage),
                    StaleOutputCleaner(fileToFqnMapInMemoryStorage),
                    eventReporter
                )

            val exitCode = incrementalJavaCompilerRunner.compile(incrementalJavaCompilerContext)

            if (exitCode == ExitCode.OK) {
                fileDigestInMemoryStorage.close()
                classpathDigestInMemoryStorage.close()
                fileToFqnMapInMemoryStorage.close()
                dependencyMapInMemoryStorage.close()

                incrementalJavaCompilerCommand.directory.deleteRecursively()
                incrementalJavaCompilerContext.outputDir.copyRecursively(incrementalJavaCompilerCommand.directory)
            } else {
                incrementalJavaCompilerContext.outputDir.deleteRecursively()
                if (incrementalJavaCompilerCommand.directory.exists()) {
                    incrementalJavaCompilerCommand.directory.copyRecursively(incrementalJavaCompilerContext.outputDir)
                }
            }

            return exitCode
        }

        private fun createCommand(args: Array<String>): IncrementalJavaCompilerCommand {
            val incrementalJavaCompilerCommand = IncrementalJavaCompilerCommand()
            val parser = CmdLineParser(incrementalJavaCompilerCommand)

            try {
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