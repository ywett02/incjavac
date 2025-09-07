//package com.example.assignment.analysis
//
//import com.example.assignment.entity.FileChanges
//import com.example.assignment.storage.FileDigestInMemoryStorage
//import com.example.assignment.util.md5
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.io.TempDir
//import org.mockito.kotlin.argThat
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.verify
//import org.mockito.kotlin.whenever
//import java.io.File
//import kotlin.test.assertEquals
//
//class FileChangesCalculatorTest {
//
//    @TempDir
//    lateinit var tempDir: File
//
//    private lateinit var mockStorage: FileDigestInMemoryStorage
//    private lateinit var calculator: FileChangesTracker
//
//    @BeforeEach
//    fun setUp() {
//        mockStorage = mock()
//        calculator = FileChangesTracker(mockStorage)
//    }
//
//    @Test
//    fun `when no source files then no changes`() {
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.trackFileChanges(emptySet())
//
//        assertEquals(FileChanges(emptySet(), emptySet()), result)
//    }
//
//    @Test
//    fun `when source files are new then all files are added`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file1, file2), emptySet()), result)
//    }
//
//    @Test
//    fun `when source files are unchanged then no changes`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(emptySet(), emptySet()), result)
//    }
//
//    @Test
//    fun `when source files are modified then files are marked as modified`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file1.absoluteFile to "oldHash1", file2.absoluteFile to "oldHash2")
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file1, file2), emptySet()), result)
//    }
//
//    @Test
//    fun `when some files are modified and others unchanged then only modified files are returned`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to "oldHash2")
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file2), emptySet()), result)
//    }
//
//    @Test
//    fun `when files are removed then files are marked as removed`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(emptySet(), setOf(file2)), result)
//    }
//
//    @Test
//    fun `when files are added and removed then both changes are detected`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val file3 = createTempFile("test3.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file2.absoluteFile to file2.md5, file3.absoluteFile to file3.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file1), setOf(file3)), result)
//    }
//
//
//    @Test
//    fun `when file content changes then file is marked as modified`() {
//        val file = createTempFile("test.java")
//        val previousMetadata = mapOf(file.absoluteFile to file.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//        file.writeText("modified content")
//        val newSourceFiles = setOf(file)
//
//        val result = calculator.trackFileChanges(newSourceFiles)
//
//        assertEquals(FileChanges(setOf(file), emptySet()), result)
//    }
//
//    @Test
//    fun `when file is renamed then old file is removed and new file is added`() {
//        val oldFile = createTempFile("old.java")
//        val newFile = createTempFile("new.java")
//        val sourceFiles = setOf(newFile)
//        val previousMetadata = mapOf(oldFile.absoluteFile to oldFile.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(newFile), setOf(oldFile)), result)
//    }
//
//    @Test
//    fun `when file is moved to different directory then old file is removed and new file is added`() {
//        val oldFile = createTempFile("old.java")
//        val newDir = File(tempDir, "subdir")
//        newDir.mkdirs()
//        val newFile = File(newDir, "old.java")
//        newFile.writeText(oldFile.readText())
//        val sourceFiles = setOf(newFile)
//        val previousMetadata = mapOf(oldFile.absoluteFile to oldFile.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(newFile), setOf(oldFile)), result)
//    }
//
//    @Test
//    fun `when file has same content but different path then both are treated as separate files`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        file2.writeText(file1.readText()) // Same content
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file2), emptySet()), result)
//    }
//
//    @Test
//    fun `when storage has files not in current source then those files are removed`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val file3 = createTempFile("test3.java")
//        val sourceFiles = setOf(file1)
//        val previousMetadata = mapOf(
//            file1.absoluteFile to file1.md5,
//            file2.absoluteFile to file2.md5,
//            file3.absoluteFile to file3.md5
//        )
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(emptySet(), setOf(file2, file3)), result)
//    }
//
//    @Test
//    fun `when all files are removed then all files are marked as removed`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = emptySet<File>()
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(emptySet(), setOf(file1, file2)), result)
//    }
//
//    @Test
//    fun `when file hash is same but file object is different then files are treated separately`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        file2.writeText(file1.readText()) // Same content, different file
//        val sourceFiles = setOf(file2)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file2), setOf(file1)), result)
//    }
//
//    @Test
//    fun `when multiple files have same content then all are processed correctly`() {
//        val content = "public class Test {}"
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val file3 = createTempFile("test3.java")
//        file1.writeText(content)
//        file2.writeText(content)
//        file3.writeText(content)
//        val sourceFiles = setOf(file1, file2, file3)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.trackFileChanges(sourceFiles)
//
//        assertEquals(FileChanges(setOf(file1, file2, file3), emptySet()), result)
//    }
//
//    @Test
//    fun `when no source files then store empty metadata`() {
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.trackFileChanges(emptySet())
//
//        verify(mockStorage).set(emptyMap())
//    }
//
//    @Test
//    fun `when source files are new then store correct metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5))
//    }
//
//    @Test
//    fun `when source files are unchanged then store same metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        val expectedMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5)
//        whenever(mockStorage.get()).thenReturn(expectedMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(expectedMetadata)
//    }
//
//    @Test
//    fun `when source files are modified then store updated metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file1.absoluteFile to "oldHash1", file2.absoluteFile to "oldHash2")
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5))
//    }
//
//    @Test
//    fun `when some files are modified and others unchanged then store correct metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to "oldHash2")
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5))
//    }
//
//    @Test
//    fun `when files are removed then store only remaining files metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1)
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5))
//    }
//
//    @Test
//    fun `when files are added and removed then store correct metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val file3 = createTempFile("test3.java")
//        val sourceFiles = setOf(file1, file2)
//        val previousMetadata = mapOf(file2.absoluteFile to file2.md5, file3.absoluteFile to file3.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5))
//    }
//
//    @Test
//    fun `when file content changes then store updated metadata`() {
//        val file = createTempFile("test.java")
//        val previousMetadata = mapOf(file.absoluteFile to file.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//        file.writeText("modified content")
//        val newSourceFiles = setOf(file)
//
//        calculator.trackFileChanges(newSourceFiles)
//
//        verify(mockStorage).set(mapOf(file.absoluteFile to file.md5))
//    }
//
//    @Test
//    fun `when file is renamed then store new file metadata`() {
//        val oldFile = createTempFile("old.java")
//        val newFile = createTempFile("new.java")
//        val sourceFiles = setOf(newFile)
//        val previousMetadata = mapOf(oldFile.absoluteFile to oldFile.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(newFile.absoluteFile to newFile.md5))
//    }
//
//    @Test
//    fun `when file is moved to different directory then store new file metadata`() {
//        val oldFile = createTempFile("old.java")
//        val newDir = File(tempDir, "subdir")
//        newDir.mkdirs()
//        val newFile = File(newDir, "old.java")
//        newFile.writeText(oldFile.readText())
//        val sourceFiles = setOf(newFile)
//        val previousMetadata = mapOf(oldFile.absoluteFile to oldFile.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(newFile.absoluteFile to newFile.md5))
//    }
//
//    @Test
//    fun `when file has same content but different path then store both files metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        file2.writeText(file1.readText())
//        val sourceFiles = setOf(file1, file2)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5))
//    }
//
//    @Test
//    fun `when storage has files not in current source then store only current files metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val file3 = createTempFile("test3.java")
//        val sourceFiles = setOf(file1)
//        val previousMetadata = mapOf(
//            file1.absoluteFile to file1.md5,
//            file2.absoluteFile to file2.md5,
//            file3.absoluteFile to file3.md5
//        )
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file1.absoluteFile to file1.md5))
//    }
//
//    @Test
//    fun `when all files are removed then store empty metadata`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = emptySet<File>()
//        val previousMetadata = mapOf(file1.absoluteFile to file1.md5, file2.absoluteFile to file2.md5)
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(emptyMap<File, String>())
//    }
//
//    @Test
//    fun `when multiple files have same content then store all files metadata`() {
//        val content = "public class Test {}"
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val file3 = createTempFile("test3.java")
//        file1.writeText(content)
//        file2.writeText(content)
//        file3.writeText(content)
//        val sourceFiles = setOf(file1, file2, file3)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(
//            mapOf(
//                file1.absoluteFile to file1.md5,
//                file2.absoluteFile to file2.md5,
//                file3.absoluteFile to file3.md5
//            )
//        )
//    }
//
//    @Test
//    fun `when file is modified multiple times then store latest metadata`() {
//        val file = createTempFile("test.java")
//        val sourceFiles = setOf(file)
//        val previousMetadata = mapOf(file.absoluteFile to "oldHash")
//        whenever(mockStorage.get()).thenReturn(previousMetadata)
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(mapOf(file.absoluteFile to file.md5))
//    }
//
//    @Test
//    fun `when storage is updated then metadata contains absolute file paths`() {
//        val file = createTempFile("test.java")
//        val sourceFiles = setOf(file)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(argThat { metadata ->
//            metadata.keys.all { it.isAbsolute } &&
//                    metadata.containsKey(file.absoluteFile)
//        })
//    }
//
//    @Test
//    fun `when storage is updated then metadata contains correct md5 hashes`() {
//        val file1 = createTempFile("test1.java")
//        val file2 = createTempFile("test2.java")
//        val sourceFiles = setOf(file1, file2)
//        whenever(mockStorage.get()).thenReturn(emptyMap())
//
//        calculator.trackFileChanges(sourceFiles)
//
//        verify(mockStorage).set(argThat { metadata ->
//            metadata[file1.absoluteFile] == file1.md5 &&
//                    metadata[file2.absoluteFile] == file2.md5
//        })
//    }
//
//    private fun createTempFile(name: String): File {
//        val file = File(tempDir, name)
//        file.writeText("public class ${name.removeSuffix(".java")} {}")
//        return file
//    }
//}
