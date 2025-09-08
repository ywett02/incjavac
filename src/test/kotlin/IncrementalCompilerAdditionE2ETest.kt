package com.example.assignment

import com.example.assignment.analysis.*
import com.example.assignment.analysis.constant.ConstantDependencyMapCollectorFactory
import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.TestEventRecorder
import com.example.assignment.storage.ClasspathDigestInMemoryStorage
import com.example.assignment.storage.DependencyGraphInMemoryStorage
import com.example.assignment.storage.FileDigestInMemoryStorage
import com.example.assignment.storage.FileToFqnMapInMemoryStorage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.tools.ToolProvider
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationAdditionE2ETest {

    @TempDir
    lateinit var tempDir: File
    private lateinit var srcDir: File
    private lateinit var outputDir: File
    private lateinit var metadataDir: File

    private lateinit var fileDigestStorage: FileDigestInMemoryStorage
    private lateinit var classpathDigestStorage: ClasspathDigestInMemoryStorage
    private lateinit var fileToFqnStorage: FileToFqnMapInMemoryStorage
    private lateinit var dependencyStorage: DependencyGraphInMemoryStorage

    private lateinit var context: IncrementalJavaCompilerContext

    private lateinit var eventRecorder: TestEventRecorder
    private lateinit var incrementalJavaCompilerRunner: IncrementalJavaCompilerRunner

    @BeforeEach
    fun setUp() {
        srcDir = File(tempDir, "src")
        outputDir = File(tempDir, "build/classes")
        metadataDir = File(tempDir, "build/cache")

        fileDigestStorage = FileDigestInMemoryStorage.create(metadataDir)
        classpathDigestStorage = ClasspathDigestInMemoryStorage.create(metadataDir)
        fileToFqnStorage = FileToFqnMapInMemoryStorage.create(metadataDir)
        dependencyStorage = DependencyGraphInMemoryStorage.create(metadataDir)

        val fileChangesTracker = FileChangesTracker(fileDigestStorage)
        val classpathChangesTracker = ClasspathChangesTracker(classpathDigestStorage)
        val dirtyFilesCalculator = DirtyFilesCalculator(fileToFqnStorage, dependencyStorage)
        val dependencyMapCollectorFactory = DependencyMapCollectorFactory(dependencyStorage)
        val fileToFqnMapCollectorFactory = FileToFqnMapCollectorFactory(fileToFqnStorage)
        val constantDependencyMapCollectorFactory = ConstantDependencyMapCollectorFactory(dependencyStorage)
        val staleOutputCleaner = StaleOutputCleaner(fileToFqnStorage)

        context = IncrementalJavaCompilerContext(
            src = srcDir,
            outputDir = outputDir,
            classpath = null,
            javaCompiler = ToolProvider.getSystemJavaCompiler()
        )

        eventRecorder = TestEventRecorder()
        incrementalJavaCompilerRunner = IncrementalJavaCompilerRunner(
            fileChangesTracker,
            classpathChangesTracker,
            dirtyFilesCalculator,
            dependencyMapCollectorFactory,
            fileToFqnMapCollectorFactory,
            constantDependencyMapCollectorFactory,
            staleOutputCleaner,
            eventRecorder
        )
    }

    @Test
    fun `when file added it is recompiled`() {
        createJavaFile(
            srcDir, "IndependentClass1.java", """
            package com.example.test;

            public class IndependentClass1 {
                public void method1() {
                    System.out.println("IndependentClass1.method1()");
                }
            }
        """
        )
        incrementalJavaCompilerRunner.compile(context)

        createJavaFile(
            srcDir, "IndependentClass2.java", """
            package com.example.test;

            public class IndependentClass2 {
                public void method2() {
                    System.out.println("IndependentClass2.method2()");
                }
            }
        """
        )
        eventRecorder.clear()
        context = IncrementalJavaCompilerContext(
            src = srcDir,
            outputDir = outputDir,
            classpath = null,
            javaCompiler = ToolProvider.getSystemJavaCompiler()
        )
        val compilationResult = incrementalJavaCompilerRunner.compile(context)

        assertEquals(ExitCode.OK, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty files:") }
        assertTrue { dirtyFileMessage.contains("IndependentClass2.java") }
        assertFalse { dirtyFileMessage.contains("IndependentClass1.java") }
    }

    private fun createJavaFile(srcDir: File, name: String, content: String): File {
        val packageDir = File(srcDir, "com/example/test")
        packageDir.mkdirs()

        val file = File(packageDir, name).apply {
            createNewFile()
        }
        file.writeText(content)
        return file
    }
}
