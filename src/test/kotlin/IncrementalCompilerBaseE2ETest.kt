package com.example.assignment

import com.example.assignment.analysis.*
import com.example.assignment.analysis.constant.ConstantDependencyMapCollectorFactory
import com.example.assignment.reporter.TestEventRecorder
import com.example.assignment.storage.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.tools.ToolProvider

abstract class IncrementalCompilerBaseE2ETest {

    @TempDir
    lateinit var tempDir: File
    protected lateinit var srcDir: File
    protected lateinit var outputDir: File
    protected lateinit var outputDirBackup: File
    protected lateinit var metadataDir: File

    private val testDataDir = File("src").resolve("test").resolve("kotlin").resolve("testData")
    private val testDataSrc = testDataDir.resolve("src")
    private val testDataCache = testDataDir.resolve("build").resolve("cache")
    private val testDataClasses = testDataDir.resolve("build").resolve("classes")

    private lateinit var fileDigestStorage: FileDigestInMemoryStorage
    private lateinit var classpathDigestStorage: ClasspathDigestInMemoryStorage
    private lateinit var fileToFqnStorage: FileToFqnMapInMemoryStorage
    private lateinit var fqnToFileStorage: FqnToFileMapInMemoryStorage
    private lateinit var dependencyStorage: DependencyGraphInMemoryStorage

    protected lateinit var incrementalJavaCompilerRunner: IncrementalJavaCompilerRunner
    protected lateinit var incrementalJavaCompilerContext: IncrementalJavaCompilerContext
    protected lateinit var eventRecorder: TestEventRecorder

    @BeforeEach
    fun setUp() {
        srcDir = File(tempDir, "src").apply {
            testDataSrc.copyRecursively(this)
        }
        metadataDir = File(tempDir, "build/cache").apply {
            testDataCache.copyRecursively(this)
        }
        outputDir = File(tempDir, "build/classes").apply {
            testDataClasses.copyRecursively(this)
        }
        outputDirBackup = File(tempDir, "build/cache/backup")

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
        incrementalJavaCompilerContext = IncrementalJavaCompilerContext(
            src = srcDir,
            outputDir = outputDir,
            outputDirBackup = outputDirBackup,
            classpath = null,
            javaCompiler = ToolProvider.getSystemJavaCompiler()
        )
    }
}