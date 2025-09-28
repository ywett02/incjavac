package com.example.javac.incremental

import com.example.javac.incremental.analysis.*
import com.example.javac.incremental.analysis.constant.ConstantDependencyMapCollectorFactory
import com.example.javac.incremental.entity.ExitCode
import com.example.javac.incremental.reporter.NoOpReporter
import com.example.javac.incremental.reporter.TestEventRecorder
import com.example.javac.incremental.storage.inMemory.*
import com.example.javac.incremental.transaction.CompilationTransaction
import com.example.javac.incremental.transaction.resource.asResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.tools.ToolProvider

abstract class IncrementalCompilerBaseE2ETest {

    @TempDir
    lateinit var tempDir: File

    protected lateinit var srcDir: File

    private lateinit var outputDir: File
    private lateinit var outputDirBackup: File
    private lateinit var metadataDir: File

    private lateinit var fileDigestStorage: FileDigestInMemoryStorage
    private lateinit var classpathDigestStorage: ClasspathDigestInMemoryStorage
    private lateinit var fileToFqnStorage: FileToFqnMapInMemoryStorage
    private lateinit var fqnToFileStorage: FqnToFileMapInMemoryStorage
    private lateinit var dependencyStorage: DependencyGraphInMemoryStorage

    protected lateinit var eventRecorder: TestEventRecorder
    protected lateinit var incrementalJavaCompilerRunner: IncrementalJavaCompilerRunner

    private val testSourceDir = File("src").resolve("test").resolve("kotlin").resolve("testData")
    protected lateinit var testSourceFiles: List<File>
    protected lateinit var testSourceFileNames: List<String>

    @BeforeEach
    fun setUp() {
        srcDir = File(tempDir, "src").apply {
            testSourceDir.copyRecursively(this)
            testSourceFiles = this.walk().filter { file -> file.name.endsWith(".java") }.toList()
            testSourceFileNames = testSourceFiles.map { file -> file.name.removeSuffix(".java") }.toList()
        }
        outputDir = File(tempDir, "build/classes")
        outputDirBackup = File(tempDir, "build/cache/backup")
        metadataDir = File(tempDir, "build/cache")

        fileDigestStorage = FileDigestInMemoryStorage.create(metadataDir)
        classpathDigestStorage = ClasspathDigestInMemoryStorage.create(metadataDir)
        fileToFqnStorage = FileToFqnMapInMemoryStorage.create(metadataDir)
        fqnToFileStorage = FqnToFileMapInMemoryStorage.create(metadataDir)
        dependencyStorage = DependencyGraphInMemoryStorage.create(metadataDir)

        val fileChangesTracker = FileChangesTracker(fileDigestStorage)
        val classpathChangesTracker = ClasspathChangesTracker(classpathDigestStorage)
        val dirtyFilesCalculator = DirtyFilesCalculator(fileToFqnStorage, fqnToFileStorage, dependencyStorage)
        val dependencyMapCollectorFactory = DependencyMapCollectorFactory(dependencyStorage)
        val fileToFqnMapCollectorFactory = FileToFqnMapCollectorFactory(fileToFqnStorage, fqnToFileStorage)
        val constantDependencyMapCollectorFactory = ConstantDependencyMapCollectorFactory(dependencyStorage)

        eventRecorder = TestEventRecorder()
        incrementalJavaCompilerRunner = IncrementalJavaCompilerRunner(
            fileChangesTracker,
            classpathChangesTracker,
            dirtyFilesCalculator,
            dependencyMapCollectorFactory,
            fileToFqnMapCollectorFactory,
            constantDependencyMapCollectorFactory,
            eventRecorder
        )

        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())
        require(compilationResult == ExitCode.OK)
    }

    protected fun createIncrementalJavaCompilerContext(): IncrementalJavaCompilerContext {
        val resourceManager = CompilationTransaction(NoOpReporter).apply {
            registerResource(fileDigestStorage.asResource())
            registerResource(classpathDigestStorage.asResource())
            registerResource(fileToFqnStorage.asResource())
            registerResource(fqnToFileStorage.asResource())
            registerResource(dependencyStorage.asResource())
        }

        return IncrementalJavaCompilerContext(
            src = srcDir,
            outputDir = outputDir,
            classpath = null,
            javaCompiler = ToolProvider.getSystemJavaCompiler(),
            compilationTransaction = resourceManager
        )
    }

    protected fun createFile(srcDir: File, name: String, content: String): File {
        val packageDir = File(srcDir, "com/example/test")
        packageDir.mkdirs()

        val file = File(packageDir, name).apply {
            createNewFile()
        }
        file.writeText(content)
        return file
    }
}