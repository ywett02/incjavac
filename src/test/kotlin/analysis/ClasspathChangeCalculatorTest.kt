//package com.example.assignment.analysis
//
//import com.example.assignment.storage.ClasspathDigestInMemoryStorage
//import com.example.assignment.util.md5
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.junit.jupiter.api.io.TempDir
//import org.mockito.kotlin.argThat
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.verify
//import org.mockito.kotlin.whenever
//import java.io.File
//import kotlin.test.assertFalse
//import kotlin.test.assertTrue
//
//class ClasspathChangeCalculatorTest {
//
//    @TempDir
//    lateinit var tempDir: File
//
//    private lateinit var mockStorage: ClasspathDigestInMemoryStorage
//    private lateinit var calculator: ClasspathChangesTracker
//
//    @BeforeEach
//    fun setUp() {
//        mockStorage = mock()
//        calculator = ClasspathChangesTracker(mockStorage)
//    }
//
//    @Test
//    fun `when classpath is null and storage is empty then classpath has no changes`() {
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.hasClasspathChanged(null)
//
//        assertFalse(result)
//    }
//
//    @Test
//    fun `when classpath is null and storage has data then classpath has changed`() {
//        val existingFile = File("existing.jar")
//        whenever(mockStorage.get()).thenReturn(mapOf(existingFile to "hash1"))
//
//        val result = calculator.hasClasspathChanged(null)
//
//        assertTrue(result)
//    }
//
//    @Test
//    fun `when classpath has changed then classpath has changed`() {
//        val jarFile = createTempJarFile("test.jar")
//        val classpath = jarFile.absolutePath
//        val expectedMetadata = mapOf(jarFile.absoluteFile to jarFile.md5)
//        val previousMetadata = mapOf(File("old.jar") to "oldHash")
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.hasClasspathChanged(classpath)
//
//        assertTrue(result)
//    }
//
//    @Test
//    fun `when classpath has not changed then classpath has no changes`() {
//        val jarFile = createTempJarFile("test.jar")
//        val classpath = jarFile.absolutePath
//        val expectedMetadata = mapOf(jarFile.absoluteFile to jarFile.md5)
//        whenever(mockStorage.get()).thenReturn(expectedMetadata)
//
//        val result = calculator.hasClasspathChanged(classpath)
//
//        assertFalse(result)
//    }
//
//    @Test
//    fun `when classpath has multiple entries then classpath has changed`() {
//        val jarFile1 = createTempJarFile("test1.jar")
//        val jarFile2 = createTempJarFile("test2.jar")
//        val classpath = "${jarFile1.absolutePath}${File.pathSeparator}${jarFile2.absolutePath}"
//        val expectedMetadata = mapOf(jarFile1.absoluteFile to jarFile1.md5, jarFile2.absoluteFile to jarFile2.md5)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.hasClasspathChanged(classpath)
//
//        assertTrue(result)
//    }
//
//    @Test
//    fun `when classpath contains directory with class files then classpath has changed`() {
//        val classDir = createTempDirectoryWithClassFiles()
//        val classpath = classDir.absolutePath
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.hasClasspathChanged(classpath)
//
//        assertTrue(result)
//    }
//
//    @Test
//    fun `when classpath contains invalid entry then throw exception`() {
//        val invalidFile = File("nonexistent.txt")
//        val classpath = invalidFile.absolutePath
//
//        assertThrows<IllegalArgumentException> {
//            calculator.hasClasspathChanged(classpath)
//        }
//    }
//
//    @Test
//    fun `when classpath is empty string then throw exception`() {
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        assertThrows<IllegalArgumentException> {
//            calculator.hasClasspathChanged("")
//        }
//    }
//
//    @Test
//    fun `when classpath contains only separators then throw exception`() {
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        assertThrows<IllegalArgumentException> {
//            calculator.hasClasspathChanged("${File.pathSeparator}${File.pathSeparator}")
//        }
//    }
//
//    @Test
//    fun `when classpath contains mixed valid and invalid entries then throw exception`() {
//        val jarFile = createTempJarFile("test.jar")
//        val invalidFile = File("nonexistent.txt")
//        val classpath = "${jarFile.absolutePath}${File.pathSeparator}${invalidFile.absolutePath}"
//
//        assertThrows<IllegalArgumentException> {
//            calculator.hasClasspathChanged(classpath)
//        }
//    }
//
//    @Test
//    fun `when classpath contains nested directory structure then classpath has changed`() {
//        val rootDir = File(tempDir, "root")
//        rootDir.mkdirs()
//        val subDir1 = File(rootDir, "sub1")
//        subDir1.mkdirs()
//        val subDir2 = File(rootDir, "sub2")
//        subDir2.mkdirs()
//        File(subDir1, "Test1.class").writeText("class content 1")
//        File(subDir2, "Test2.class").writeText("class content 2")
//        File(rootDir, "RootTest.class").writeText("root class content")
//        val classpath = rootDir.absolutePath
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.hasClasspathChanged(classpath)
//
//        assertTrue(result)
//    }
//
//    @Test
//    fun `when classpath contains directory with mixed files then classpath has changed`() {
//        val classDir = File(tempDir, "mixed")
//        classDir.mkdirs()
//        File(classDir, "Test.class").writeText("class content")
//        File(classDir, "Test.java").writeText("java content")
//        File(classDir, "Test.txt").writeText("text content")
//        val classpath = classDir.absolutePath
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.hasClasspathChanged(classpath)
//
//        assertTrue(result)
//    }
//
//    // Storage verification tests
//    @Test
//    fun `when classpath is null then store empty map`() {
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(null)
//
//        verify(mockStorage).set(emptyMap<File, String>())
//    }
//
//    @Test
//    fun `when classpath contains single jar file then store with correct hash`() {
//        val jarFile = createTempJarFile("test.jar")
//        val classpath = jarFile.absolutePath
//        val expectedMetadata = mapOf(jarFile.absoluteFile to jarFile.md5)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath contains multiple jar files then store with correct hashes`() {
//        val jarFile1 = createTempJarFile("test1.jar")
//        val jarFile2 = createTempJarFile("test2.jar")
//        val classpath = "${jarFile1.absolutePath}${File.pathSeparator}${jarFile2.absolutePath}"
//        val expectedMetadata = mapOf(
//            jarFile1.absoluteFile to jarFile1.md5,
//            jarFile2.absoluteFile to jarFile2.md5
//        )
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath contains directory then store all class files with correct hashes`() {
//        val classDir = createTempDirectoryWithClassFiles()
//        val classpath = classDir.absolutePath
//        val classFile1 = File(classDir, "TestClass.class")
//        val classFile2 = File(classDir, "AnotherClass.class")
//        val expectedMetadata = mapOf(
//            classFile1.absoluteFile to classFile1.md5,
//            classFile2.absoluteFile to classFile2.md5
//        )
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath contains nested directories then store all class files with correct hashes`() {
//        val rootDir = File(tempDir, "root")
//        rootDir.mkdirs()
//        val subDir1 = File(rootDir, "sub1")
//        subDir1.mkdirs()
//        val subDir2 = File(rootDir, "sub2")
//        subDir2.mkdirs()
//
//        val classFile1 = File(subDir1, "Test1.class")
//        val classFile2 = File(subDir2, "Test2.class")
//        val classFile3 = File(rootDir, "RootTest.class")
//
//        classFile1.writeText("class content 1")
//        classFile2.writeText("class content 2")
//        classFile3.writeText("root class content")
//
//        val classpath = rootDir.absolutePath
//        val expectedMetadata = mapOf(
//            classFile1.absoluteFile to classFile1.md5,
//            classFile2.absoluteFile to classFile2.md5,
//            classFile3.absoluteFile to classFile3.md5
//        )
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath contains directory with mixed files then store only class files`() {
//        val classDir = File(tempDir, "mixed")
//        classDir.mkdirs()
//
//        val classFile = File(classDir, "Test.class")
//        val javaFile = File(classDir, "Test.java")
//        val txtFile = File(classDir, "Test.txt")
//
//        classFile.writeText("class content")
//        javaFile.writeText("java content")
//        txtFile.writeText("text content")
//
//        val classpath = classDir.absolutePath
//        val expectedMetadata = mapOf(classFile.absoluteFile to classFile.md5)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath contains mixed jar files and directories then store all with correct hashes`() {
//        val jarFile = createTempJarFile("test.jar")
//        val classDir = createTempDirectoryWithClassFiles()
//        val classpath = "${jarFile.absolutePath}${File.pathSeparator}${classDir.absolutePath}"
//
//        val classFile1 = File(classDir, "TestClass.class")
//        val classFile2 = File(classDir, "AnotherClass.class")
//
//        val expectedMetadata = mapOf(
//            jarFile.absoluteFile to jarFile.md5,
//            classFile1.absoluteFile to classFile1.md5,
//            classFile2.absoluteFile to classFile2.md5
//        )
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath contains files then store with absolute paths`() {
//        val jarFile = createTempJarFile("test.jar")
//        val classpath = jarFile.absolutePath
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(argThat { metadata ->
//            metadata.keys.all { it.isAbsolute } &&
//                    metadata.containsKey(jarFile.absoluteFile)
//        })
//    }
//
//    @Test
//    fun `when classpath has not changed then still update storage`() {
//        val jarFile = createTempJarFile("test.jar")
//        val classpath = jarFile.absolutePath
//        val expectedMetadata = mapOf(jarFile.absoluteFile to jarFile.md5)
//        whenever(mockStorage.get()).thenReturn(expectedMetadata)
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when classpath changes then store new metadata replacing existing`() {
//        val oldJarFile = createTempJarFile("old.jar")
//        val newJarFile = createTempJarFile("new.jar")
//        val classpath = newJarFile.absolutePath
//
//        val oldMetadata = mapOf(oldJarFile.absoluteFile to oldJarFile.md5)
//        val expectedNewMetadata = mapOf(newJarFile.absoluteFile to newJarFile.md5)
//        whenever(mockStorage.get()).thenReturn(oldMetadata)
//
//        calculator.hasClasspathChanged(classpath)
//
//        verify(mockStorage).set(expectedNewMetadata)
//    }
//
//    private fun createTempJarFile(name: String): File {
//        val jarFile = File(tempDir, name)
//        jarFile.writeText("fake jar content")
//        return jarFile
//    }
//
//    private fun createTempDirectoryWithClassFiles(): File {
//        val dir = File(tempDir, "classes")
//        dir.mkdirs()
//
//        File(dir, "TestClass.class").writeText("fake class content")
//        File(dir, "AnotherClass.class").writeText("another fake class content")
//
//        return dir
//    }
//}