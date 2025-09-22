package com.example.assignment

import com.example.assignment.analysis.*
import com.example.assignment.analysis.constant.ConstantDependencyMapCollectorFactory
import com.example.assignment.entity.ExitCode
import com.example.assignment.reporter.TestEventRecorder
import com.example.assignment.storage.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import javax.tools.ToolProvider
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IncrementalCompilationConstantE2ETest {

    @TempDir
    lateinit var tempDir: File
    private lateinit var srcDir: File
    private lateinit var outputDir: File
    private lateinit var outputDirBackup: File
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

        context = IncrementalJavaCompilerContext(
            src = srcDir,
            outputDir = outputDir,
            outputDirBackup = outputDirBackup,
            classpath = null,
            javaCompiler = ToolProvider.getSystemJavaCompiler(),
            onCompilationCompleted = { exitCode ->
                if (exitCode == ExitCode.OK) {
                    fileDigestStorage.close()
                    classpathDigestStorage.close()
                    fileToFqnStorage.close()
                    fqnToFileStorage.close()
                    dependencyStorage.close()
                }
            }
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
    fun `when constant is changed its user is recompiled`() {
        val constantOwner = createJavaFile(
            srcDir, "ConstantOwner.java", """
            package com.example.test;

            public class ConstantOwner {
                public static final String CONSTANT_VALUE = "constant_value";
            }
        """
        )

        createJavaFile(
            srcDir, "ConstantUser.java", """
            package com.example.test;

            public class ConstantUser {
                public void printConstantValue() {
                    System.out.println(ConstantOwner.CONSTANT_VALUE);
                }
            }
        """
        )

        createJavaFile(
            srcDir, "NotConstantUser.java", """
            package com.example.test;

            public class NotConstantUser {
                public void printConstantValue() {
                    System.out.println("no constant value");
                }
            }
        """
        )

        incrementalJavaCompilerRunner.compile(context)
        setUp()

        constantOwner.writeText(
            """
            package com.example.test;

            public class ConstantOwner {
                public static final String CONSTANT_VALUE = "constant_value_changed";
            }
        """
        )

        val compilationResult = incrementalJavaCompilerRunner.compile(context)

        assertEquals(ExitCode.OK, compilationResult)
        val dirtyFileMessage = eventRecorder.events.first { message -> message.contains("Dirty source files:") }
        assertTrue { dirtyFileMessage.contains("ConstantOwner.java") }
        assertTrue { dirtyFileMessage.contains("ConstantUser.java") }
        assertFalse { dirtyFileMessage.contains("NotConstantUser.java") }
        val dirtyClassMessage = eventRecorder.events.first { message -> message.contains("Dirty class files:") }
        assertTrue { dirtyClassMessage.contains("ConstantOwner.class") }
        assertTrue { dirtyClassMessage.contains("ConstantUser.class") }
        assertFalse { dirtyClassMessage.contains("NotConstantUser.class") }
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
