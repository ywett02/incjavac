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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.tools.ToolProvider
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationRemovalE2ETest {

    @TempDir
    lateinit var tempDir: File
    private lateinit var srcDir: File
    private lateinit var outputDir: File
    private lateinit var metadataDir: File

    private lateinit var fileDigestStorage: FileDigestInMemoryStorage
    private lateinit var classpathDigestStorage: ClasspathDigestInMemoryStorage
    private lateinit var fileToFqnStorage: FileToFqnMapInMemoryStorage
    private lateinit var fqnToFileStorage: FqnToFileMapInMemoryStorage
    private lateinit var dependencyStorage: DependencyGraphInMemoryStorage

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
    }

    @Test
    fun `when file with dependant is removed dependant is recompiled`() {
        val independentClass1 = createJavaFile(
            srcDir, "IndependentClass1.java", """
            package com.example.test;

            public class IndependentClass1 {
                public void method1() {
                    System.out.println("IndependentClass1.method1()");
                }
            }
        """
        )
        createJavaFile(
            srcDir, "DependentClass.java", """
            package com.example.test;

            public class DependentClass {
                public void method() {
                    new IndependentClass1().method1();
                }
            }
        """
        )
        incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())
        setUp()

        independentClass1.delete()
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        assertEquals(ExitCode.COMPILATION_ERROR, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("DependentClass.java") }
        assertFalse { dirtyFileMessage.contains("IndependentClass1.java") }
        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("DependentClass.class") }
        assertTrue { dirtyClassMessage.contains("IndependentClass1.class") }
    }

    @Test
    fun `when file with no dependant is nothing is recompiled`() {
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
        val dependentClass = createJavaFile(
            srcDir, "DependentClass.java", """
            package com.example.test;

            public class DependentClass {
                public void method() {
                    new IndependentClass1().method1();
                }
            }
        """
        )
        incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())
        setUp()

        dependentClass.delete()
        val compilationResult = incrementalJavaCompilerRunner.compile(createIncrementalJavaCompilerContext())

        assertEquals(ExitCode.OK, compilationResult)
        eventRecorder.events.first { message -> message.contains("Dirty source files: []") }
        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("DependentClass.class") }
        assertFalse { dirtyClassMessage.contains("IndependentClass1.class") }
    }

    private fun createIncrementalJavaCompilerContext(): IncrementalJavaCompilerContext {
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
