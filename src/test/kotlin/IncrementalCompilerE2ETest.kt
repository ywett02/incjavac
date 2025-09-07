//package com.example.assignment
//
//import com.example.assignment.analysis.*
//import com.example.assignment.analysis.constant.ConstantDependencyMapCollectorFactory
//import com.example.assignment.entity.ExitCode
//import com.example.assignment.storage.*
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.io.TempDir
//import java.io.File
//import javax.tools.ToolProvider
//import kotlin.test.assertEquals
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//class IncrementalCompilationE2ETest {
//
//    @TempDir
//    lateinit var tempDir: File
//    private lateinit var srcDir: File
//    private lateinit var outputDir: File
//    private lateinit var metadataDir: File
//
//    private lateinit var fileDigestStorage: FileDigestInMemoryStorage
//    private lateinit var classpathDigestStorage: ClasspathDigestInMemoryStorage
//    private lateinit var fileToFqnStorage: FileToFqnMapInMemoryStorage
//    private lateinit var dependencyStorage: DependencyMapInMemoryStorage
//
//    private lateinit var eventRecorder: TestEventRecorder
//    private lateinit var incrementalJavaCompilerRunner: IncrementalJavaCompilerRunner
//
//    @BeforeEach
//    fun setUp() {
//        eventRecorder = TestEventRecorder()
//
//        srcDir = File(tempDir, "src")
//        outputDir = File(tempDir, "build/classes")
//        metadataDir = File(tempDir, "build/cache/metadata")
//
//        fileDigestStorage = FileDigestInMemoryStorage.create(metadataDir)
//        classpathDigestStorage = ClasspathDigestInMemoryStorage.create(metadataDir)
//        fileToFqnStorage = FileToFqnMapInMemoryStorage.create(metadataDir)
//        dependencyStorage = DependencyMapInMemoryStorage.create(metadataDir)
//
//        val fileChangesCalculator = FileChangesCalculator(fileDigestStorage)
//        val classpathChangeCalculator = ClasspathChangeCalculator(classpathDigestStorage)
//        val dirtyFilesCalculator = DirtyFilesCalculator(fileToFqnStorage, dependencyStorage)
//        val dependencyMapCollectorFactory = DependencyMapCollectorFactory(dependencyStorage)
//        val fileToFqnMapCollectorFactory = FileToFqnMapCollectorFactory(fileToFqnStorage)
//        val constantDependencyMapCollectorFactory = ConstantDependencyMapCollectorFactory(dependencyStorage)
//        val staleOutputCleaner = StaleOutputCleaner(fileToFqnStorage, dependencyStorage)
//
//        incrementalJavaCompilerRunner = IncrementalJavaCompilerRunner(
//            fileChangesCalculator,
//            classpathChangeCalculator,
//            dirtyFilesCalculator,
//            dependencyMapCollectorFactory,
//            fileToFqnMapCollectorFactory,
//            constantDependencyMapCollectorFactory,
//            staleOutputCleaner,
//            eventRecorder
//        )
//    }
//
//    @Test
//    fun `when first run, non-incremental mode is triggered`() {
//        createJavaFile(
//            srcDir, "IndependentClass1.java", """
//            package com.example.test;
//
//            public class IndependentClass1 {
//                public void method1() {
//                    System.out.println("IndependentClass1.method1()");
//                }
//            }
//        """
//        )
//
//        createJavaFile(
//            srcDir, "IndependentClass2.java", """
//            package com.example.test;
//
//            public class IndependentClass2 {
//                public void method2() {
//                    System.out.println("IndependentClass2.method2()");
//                }
//            }
//        """
//        )
//
//        val context = IncrementalJavaCompilerContext(
//            src = srcDir,
//            outputDir = outputDir,
//            metadataDir = metadataDir,
//            classpath = null,
//            javaCompiler = ToolProvider.getSystemJavaCompiler()
//        )
//        val compilationResult = incrementalJavaCompilerRunner.compile(context)
//
//        assertEquals(ExitCode.OK, compilationResult)
//        assertTrue(
//            eventRecorder.getMessages()
//                .any { it.contains("Non-incremental compilation will be performed: Required metadata doest not exist") })
//    }
//
//    @Test
//    fun `when changing a file with no dependencies then only that file should be recompiled`() {
//        val independentClass1 = createJavaFile(
//            srcDir, "IndependentClass1.java", """
//            package com.example.test;
//
//            public class IndependentClass1 {
//                public void method1() {
//                    System.out.println("IndependentClass1.method1()");
//                }
//            }
//        """
//        )
//
//        val independentClass2 = createJavaFile(
//            srcDir, "IndependentClass2.java", """
//            package com.example.test;
//
//            public class IndependentClass2 {
//                public void method2() {
//                    System.out.println("IndependentClass2.method2()");
//                }
//            }
//        """
//        )
//        val context = IncrementalJavaCompilerContext(
//            src = srcDir,
//            outputDir = outputDir,
//            metadataDir = metadataDir,
//            classpath = null,
//            javaCompiler = ToolProvider.getSystemJavaCompiler()
//        )
//        incrementalJavaCompilerRunner.compile(context)
//        fileDigestStorage.close()
//        classpathDigestStorage.close()
//        fileToFqnStorage.close()
//        dependencyStorage.close()
//        eventRecorder.clearMessages()
//
//        independentClass2.writeText(
//            """
//            package com.example.test;
//
//            public class IndependentClass2 {
//                public void method2() {
//                    System.out.println("IndependentClass2.method2() - MODIFIED");
//                }
//
//                public void newMethod() {
//                    System.out.println("New method added");
//                }
//            }
//        """
//        )
//        val compilationResult = incrementalJavaCompilerRunner.compile(context)
//
//        assertEquals(ExitCode.OK, compilationResult)
//
//        val dirtyFileMessage = eventRecorder.getMessages().first { message -> message.contains("Dirty files:") }
//        assertTrue { dirtyFileMessage.contains("IndependentClass2.java") }
//        assertFalse { dirtyFileMessage.contains("IndependentClass1.java") }
//    }
//
//    private fun createJavaFile(srcDir: File, name: String, content: String): File {
//        val packageDir = File(srcDir, "com/example/test")
//        packageDir.mkdirs()
//
//        val file = File(packageDir, name).apply {
//            createNewFile()
//        }
//        file.writeText(content)
//        return file
//    }
//}
//
