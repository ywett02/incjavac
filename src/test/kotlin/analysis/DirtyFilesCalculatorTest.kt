//package com.example.assignment.analysis
//
//import com.example.assignment.entity.FileChanges
//import com.example.assignment.entity.FqName
//import com.example.assignment.storage.DependencyMapInMemoryStorage
//import com.example.assignment.storage.FileToFqnMapInMemoryStorage
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.io.TempDir
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.whenever
//import java.io.File
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class DirtyFilesCalculatorTest {
//
//    @TempDir
//    lateinit var tempDir: File
//
//    private lateinit var mockFileToFqnStorage: FileToFqnMapInMemoryStorage
//    private lateinit var mockDependencyStorage: DependencyMapInMemoryStorage
//    private lateinit var calculator: DirtyFilesCalculator
//
//    @BeforeEach
//    fun setUp() {
//        mockFileToFqnStorage = mock()
//        mockDependencyStorage = mock()
//        calculator = DirtyFilesCalculator(mockFileToFqnStorage, mockDependencyStorage)
//    }
//
//    @Test
//    fun `when no file changes then no dirty files`() {
//        val changes = FileChanges(emptySet(), emptySet())
//        whenever(mockFileToFqnStorage.get()).thenReturn(emptyMap())
//        whenever(mockDependencyStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertTrue(result.isEmpty())
//    }
//
//    @Test
//    fun `when files are added and modified then return those files`() {
//        val file1 = File("src/Test1.java")
//        val file2 = File("src/Test2.java")
//        val changes = FileChanges(setOf(file1, file2), emptySet())
//        whenever(mockFileToFqnStorage.get()).thenReturn(emptyMap())
//        whenever(mockDependencyStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(file1, file2), result)
//    }
//
//    @Test
//    fun `when files are removed then return empty set`() {
//        val file1 = File("src/Test1.java")
//        val file2 = File("src/Test2.java")
//        val changes = FileChanges(emptySet(), setOf(file1, file2))
//        whenever(mockFileToFqnStorage.get()).thenReturn(emptyMap())
//        whenever(mockDependencyStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertTrue(result.isEmpty())
//    }
//
//    @Test
//    fun `when files are added and removed then return only added files`() {
//        val addedFile = File("src/NewTest.java")
//        val removedFile = File("src/OldTest.java")
//        val changes = FileChanges(setOf(addedFile), setOf(removedFile))
//        whenever(mockFileToFqnStorage.get()).thenReturn(emptyMap())
//        whenever(mockDependencyStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(addedFile), result)
//    }
//
//    @Test
//    fun `when file has dependencies then return dependent files`() {
//        val sourceFile = File("src/Source.java")
//        val dependentFile = File("src/Dependent.java")
//        val sourceFqn = FqName("com.example.Source")
//        val dependentFqn = FqName("com.example.Dependent")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(
//            sourceFile to setOf(sourceFqn),
//            dependentFile to setOf(dependentFqn)
//        )
//        val dependencyMap = mapOf(dependentFqn to setOf(sourceFqn))
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile, dependentFile), result)
//    }
//
//    @Test
//    fun `when file has transitive dependencies then return only direct dependent files`() {
//        val sourceFile = File("src/Source.java")
//        val directDependentFile = File("src/DirectDependent.java")
//        val transitiveDependentFile = File("src/TransitiveDependent.java")
//        val sourceFqn = FqName("com.example.Source")
//        val directFqn = FqName("com.example.DirectDependent")
//        val transitiveFqn = FqName("com.example.TransitiveDependent")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(
//            sourceFile to setOf(sourceFqn),
//            directDependentFile to setOf(directFqn),
//            transitiveDependentFile to setOf(transitiveFqn)
//        )
//        val dependencyMap = mapOf(
//            directFqn to setOf(sourceFqn),
//            transitiveFqn to setOf(directFqn)
//        )
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile, directDependentFile), result)
//    }
//
//    @Test
//    fun `when multiple files have dependencies then return all dependent files`() {
//        val file1 = File("src/File1.java")
//        val file2 = File("src/File2.java")
//        val dependentFile1 = File("src/Dependent1.java")
//        val dependentFile2 = File("src/Dependent2.java")
//        val fqn1 = FqName("com.example.File1")
//        val fqn2 = FqName("com.example.File2")
//        val depFqn1 = FqName("com.example.Dependent1")
//        val depFqn2 = FqName("com.example.Dependent2")
//        val changes = FileChanges(setOf(file1, file2), emptySet())
//        val fileToFqnMap = mapOf(
//            file1 to setOf(fqn1),
//            file2 to setOf(fqn2),
//            dependentFile1 to setOf(depFqn1),
//            dependentFile2 to setOf(depFqn2)
//        )
//        val dependencyMap = mapOf(
//            depFqn1 to setOf(fqn1),
//            depFqn2 to setOf(fqn2)
//        )
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(file1, file2, dependentFile1, dependentFile2), result)
//    }
//
//    @Test
//    fun `when file has no dependencies then return only source file`() {
//        val sourceFile = File("src/Source.java")
//        val sourceFqn = FqName("com.example.Source")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(sourceFile to setOf(sourceFqn))
//        val dependencyMap = mapOf(sourceFqn to emptySet<FqName>())
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile), result)
//    }
//
//    @Test
//    fun `when file is not in file to fqn map then return only source file`() {
//        val sourceFile = File("src/Source.java")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        whenever(mockFileToFqnStorage.get()).thenReturn(emptyMap())
//        whenever(mockDependencyStorage.get()).thenReturn(emptyMap())
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile), result)
//    }
//
//    @Test
//    fun `when dependency has no corresponding file then return only source file`() {
//        val sourceFile = File("src/Source.java")
//        val sourceFqn = FqName("com.example.Source")
//        val dependentFqn = FqName("com.example.Dependent")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(sourceFile to setOf(sourceFqn))
//        val dependencyMap = mapOf(sourceFqn to setOf(dependentFqn))
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile), result)
//    }
//
//    @Test
//    fun `when file has multiple fqns then return all dependent files`() {
//        val sourceFile = File("src/Source.java")
//        val dependentFile1 = File("src/Dependent1.java")
//        val dependentFile2 = File("src/Dependent2.java")
//        val fqn1 = FqName("com.example.Source1")
//        val fqn2 = FqName("com.example.Source2")
//        val depFqn1 = FqName("com.example.Dependent1")
//        val depFqn2 = FqName("com.example.Dependent2")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(
//            sourceFile to setOf(fqn1, fqn2),
//            dependentFile1 to setOf(depFqn1),
//            dependentFile2 to setOf(depFqn2)
//        )
//        val dependencyMap = mapOf(
//            depFqn1 to setOf(fqn1),
//            depFqn2 to setOf(fqn2)
//        )
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile, dependentFile1, dependentFile2), result)
//    }
//
//    @Test
//    fun `when dependency has multiple files then return all files`() {
//        val sourceFile = File("src/Source.java")
//        val dependentFile1 = File("src/Dependent1.java")
//        val dependentFile2 = File("src/Dependent2.java")
//        val sourceFqn = FqName("com.example.Source")
//        val dependentFqn = FqName("com.example.Dependent")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(
//            sourceFile to setOf(sourceFqn),
//            dependentFile1 to setOf(dependentFqn),
//            dependentFile2 to setOf(dependentFqn)
//        )
//        val dependencyMap = mapOf(dependentFqn to setOf(sourceFqn))
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile, dependentFile1, dependentFile2), result)
//    }
//
//    @Test
//    fun `when files are added and removed with dependencies then return correct files`() {
//        val addedFile = File("src/Added.java")
//        val removedFile = File("src/Removed.java")
//        val dependentFile = File("src/Dependent.java")
//        val addedFqn = FqName("com.example.Added")
//        val dependentFqn = FqName("com.example.Dependent")
//        val changes = FileChanges(setOf(addedFile), setOf(removedFile))
//        val fileToFqnMap = mapOf(
//            addedFile to setOf(addedFqn),
//            dependentFile to setOf(dependentFqn)
//        )
//        val dependencyMap = mapOf(dependentFqn to setOf(addedFqn))
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(addedFile, dependentFile), result)
//    }
//
//    @Test
//    fun `when circular dependencies exist then return all files in cycle`() {
//        val file1 = File("src/File1.java")
//        val file2 = File("src/File2.java")
//        val fqn1 = FqName("com.example.File1")
//        val fqn2 = FqName("com.example.File2")
//        val changes = FileChanges(setOf(file1), emptySet())
//        val fileToFqnMap = mapOf(
//            file1 to setOf(fqn1),
//            file2 to setOf(fqn2)
//        )
//        val dependencyMap = mapOf(
//            fqn2 to setOf(fqn1),
//            fqn1 to setOf(fqn2)
//        )
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(file1, file2), result)
//    }
//
//    @Test
//    fun `when complex dependency graph exists then return only direct dependent files`() {
//        val sourceFile = File("src/Source.java")
//        val intermediateFile1 = File("src/Intermediate1.java")
//        val intermediateFile2 = File("src/Intermediate2.java")
//        val finalFile1 = File("src/Final1.java")
//        val finalFile2 = File("src/Final2.java")
//        val sourceFqn = FqName("com.example.Source")
//        val intermediateFqn1 = FqName("com.example.Intermediate1")
//        val intermediateFqn2 = FqName("com.example.Intermediate2")
//        val finalFqn1 = FqName("com.example.Final1")
//        val finalFqn2 = FqName("com.example.Final2")
//        val changes = FileChanges(setOf(sourceFile), emptySet())
//        val fileToFqnMap = mapOf(
//            sourceFile to setOf(sourceFqn),
//            intermediateFile1 to setOf(intermediateFqn1),
//            intermediateFile2 to setOf(intermediateFqn2),
//            finalFile1 to setOf(finalFqn1),
//            finalFile2 to setOf(finalFqn2)
//        )
//        val dependencyMap = mapOf(
//            intermediateFqn1 to setOf(sourceFqn),
//            intermediateFqn2 to setOf(sourceFqn),
//            finalFqn1 to setOf(intermediateFqn1),
//            finalFqn2 to setOf(intermediateFqn2)
//        )
//
//        whenever(mockFileToFqnStorage.get()).thenReturn(fileToFqnMap)
//        whenever(mockDependencyStorage.get()).thenReturn(dependencyMap)
//
//        val result = calculator.calculateDirtyFiles(changes)
//
//        assertEquals(setOf(sourceFile, intermediateFile1, intermediateFile2), result)
//    }
//}
