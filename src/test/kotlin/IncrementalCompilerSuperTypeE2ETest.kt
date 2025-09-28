package com.example.assignment

import com.example.assignment.analysis.*
import com.example.assignment.analysis.constant.ConstantDependencyMapCollectorFactory
import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.NoOpReporter
import com.example.assignment.reporter.TestEventRecorder
import com.example.assignment.storage.inMemory.*
import com.example.assignment.transaction.CompilationTransaction
import com.example.assignment.transaction.impl.asResource
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.tools.ToolProvider
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationSuperTypesE2ETest {

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
        fqnToFileStorage = FqnToFileMapInMemoryStorage.create(metadataDir)
        dependencyStorage = DependencyGraphInMemoryStorage.create(metadataDir)

        val fileChangesTracker = FileChangesTracker(fileDigestStorage)
        val classpathChangesTracker = ClasspathChangesTracker(classpathDigestStorage)
        val dirtyFilesCalculator = DirtyFilesCalculator(fileToFqnStorage, fqnToFileStorage, dependencyStorage)
        val dependencyMapCollectorFactory = DependencyMapCollectorFactory(dependencyStorage)
        val fileToFqnMapCollectorFactory = FileToFqnMapCollectorFactory(fileToFqnStorage, fqnToFileStorage)
        val constantDependencyMapCollectorFactory = ConstantDependencyMapCollectorFactory(dependencyStorage)

        val resourceManager = CompilationTransaction(NoOpReporter).apply {
            registerResource(fileDigestStorage.asResource())
            registerResource(classpathDigestStorage.asResource())
            registerResource(fileToFqnStorage.asResource())
            registerResource(fqnToFileStorage.asResource())
            registerResource(dependencyStorage.asResource())
        }

        context = IncrementalJavaCompilerContext(
            src = srcDir,
            outputDir = outputDir,
            classpath = null,
            javaCompiler = ToolProvider.getSystemJavaCompiler(),
            compilationTransaction = resourceManager
        )

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
    fun `when supertype is changed all transitive children are recompiled`() {
        val interfaceFile = createJavaFile(
            srcDir, "InterfaceTest.java", """
            package com.example.test;

            public interface InterfaceTest {
                void interfaceMethod();
            }
        """
        )

        createJavaFile(
            srcDir, "AbstractClassTest.java", """
            package com.example.test;

            public abstract class AbstractClassTest implements InterfaceTest {
                abstract void abstractClassMethod();
            }
        """
        )

        createJavaFile(
            srcDir, "ClassTest.java", """
            package com.example.test;

            public class ClassTest extends AbstractClassTest {
                @Override
                public void abstractClassMethod() {
                    System.out.println("abstractClassMethod()");
                }
                
                @Override
                public void interfaceMethod() {
                    System.out.println("interfaceMethod()");
                }
            }
        """
        )

        createJavaFile(
            srcDir, "ClassTest2.java", """
            package com.example.test;

            public class ClassTest2  {
                public void classMethod() {
                    System.out.println("classMethod()");
                }
            }
        """
        )
        incrementalJavaCompilerRunner.compile(context)
        setUp()

        interfaceFile.writeText(
            """
            package com.example.test;

            public interface InterfaceTest {
                void interfaceMethod();
                
                void secondInterfaceMethod();
            }
        """
        )

        val compilationResult = incrementalJavaCompilerRunner.compile(context)

        assertEquals(ExitCode.COMPILATION_ERROR, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("InterfaceTest.java") }
        assertTrue { dirtyFileMessage.contains("AbstractClassTest.java") }
        assertTrue { dirtyFileMessage.contains("ClassTest.java") }
        assertFalse { dirtyFileMessage.contains("ClassTest2.java") }
        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("InterfaceTest.class") }
        assertTrue { dirtyClassMessage.contains("AbstractClassTest.class") }
        assertTrue { dirtyClassMessage.contains("ClassTest.class") }
        assertFalse { dirtyClassMessage.contains("ClassTest2.class") }
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
